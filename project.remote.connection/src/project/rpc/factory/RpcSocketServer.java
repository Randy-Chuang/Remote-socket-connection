package project.rpc.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import project.rpc.factory.format.IFormatProcessor;
import project.rpc.factory.format.JsonFormatProcessor;
import project.rpc.factory.protocol.AbstractProtocolProcessor;
import project.rpc.factory.protocol.DefaultProtocolProcessor;

/**
 * The implementation of socket server that provides client handling in multi-threading.   
 *
 */
public class RpcSocketServer implements IRpcServer {
	private ServerSocket serverSocket;
	private final Map<String, Invocable<?>> serviceMap;
	private Thread serverThread; 
	private ExecutorService threadPool; 
	
	// default value, shall be designated by user
	private IFormatProcessor formatProcessor = null;
	private Class<? extends IFormatProcessor> formatProcessorClass = JsonFormatProcessor.class;
	private final Map<String, Class<?>> paramClassMap = new TreeMap<>();
	private Class<? extends AbstractProtocolProcessor> protocolProcessorClass = DefaultProtocolProcessor.class;
	
	/**
	 * Initialize the socket server with a given serving port. 
	 * @param portNumber the port number of the socket server. 
	 * @throws IOException if fail to create the ServerSocket due to some reasons (i.e. occupied port).
	 */
	public RpcSocketServer(final int portNumber) throws IOException {
		this.serverSocket = new ServerSocket(portNumber);
		this.serviceMap = new TreeMap<String, Invocable<?>>();
		this.threadPool = Executors.newFixedThreadPool(5);
	}
	
	/**
	 * Setup the class type of protocol processor used for encoding / decoding protocolary message in client handling. 
	 * @param type the Class type used for creating protocol processor.
	 * @return the instance of this socket server. 
	 */
	public RpcSocketServer setProtocolProcessor(Class<? extends AbstractProtocolProcessor> type) {
		if(type.getCanonicalName().equals(AbstractProtocolProcessor.class.getCanonicalName())) {
			System.err.println(AbstractProtocolProcessor.class.getCanonicalName() + ", the base abstract class contains "
					+ "several abstract methods which are not compatible in practice of protocol processing and control.");
			System.err.println(protocolProcessorClass.getCanonicalName() + ", the default protocol process would be adopted instead.");
		}
		else {
			protocolProcessorClass = type;
		}
		return this;
	}
	
	/**
	 * Setup the class type of format processor used for encoding/decoding service request and reply. 
	 * @param type the Class type used for creating format processor.
	 * @return the instance of this socket server. 
	 */
	public RpcSocketServer setFormatProcessor(Class<? extends IFormatProcessor> type) {
		if(type.getCanonicalName().equals(IFormatProcessor.class.getCanonicalName())) {
			System.err.println(IFormatProcessor.class.getCanonicalName() + ", the base interface contains "
					+ "several unimplemented methods which are not compatible in practice of format processing.");
			System.err.println(formatProcessorClass.getCanonicalName() + ", the default format process would be adopted instead.");
		}
		else {
			formatProcessorClass = type;
		}
		return this;
	}
	
	/**
	 * Start the socket server.
	 */	
	@Override
	public void start() {
		// Create a FormatProcessor. 
		try {
			formatProcessor = formatProcessorClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			formatProcessor = null;
			e.printStackTrace();
			return;
		}
		
		// Create server thread (incoming connection request would be handled in multi-threading).
		serverThread = new Thread(new Runnable() {
			// Thread task for the socket server.
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					Socket s = null;
					try {
						// Wait and accept for incoming client socket request 
						// (Blocking state: block until a new connection is established).
						s = serverSocket.accept();
						System.out.println("A new client is connected : " + s);
						System.out.println("Assigning new thread for this client");

						// create a new Thread (from Runnable) used for client handling. 
						try {
							Runnable runnable = new ClientHandlerRunnable(s);
							threadPool.execute(runnable);
						} catch (Exception e) { 
							s.close();
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// Start server thread. 
		serverThread.start();
	}
	
	/**
     * Stop the socket server. 
     */
	@Override
	public void stop() {
		System.out.println("Closing server.");
		// set interrupt flag for all actively executing thread and wait all threads to terminate.
		threadPool.shutdownNow();
		while (true) {
			try {
				System.out.println("Waiting for the service to terminate...");
				if (threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Close ServerSocket in order to terminate the blocking state of ServerSocket.accept(). 
		try {
			serverSocket.close();
			System.out.println("ServerSocket closed.");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// Close server thread.
		serverThread.interrupt();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Server closed.");
	}
	
	/**
     * Add a service to the socket server. 
     * @param <T> the parameter type for this service. 
     * @param name the name of service. 
     * @param paramClass the class of parameter type for this service. 
     * @param r the actual instance that are going to handle this service. 
     */
	@Override
	public <T> void addRequestHandler(String name, Class<T> paramClass, Invocable <T> r) {
		// Check for duplication from existing services. 
		if(serviceMap.containsKey(name)) {
			System.err.println("The existing service will be overridden: " + name);
		}
		// Add service.
		serviceMap.put(name, r);
		// Add Class type of service parameter. 
		paramClassMap.put(name, paramClass);
	}
	
	/**
	 * Instance of ClientHandlerRunnable will be executed by a dedicated thread to provide server services 
	 * to each remote client. 
	 */
	private class ClientHandlerRunnable implements Runnable {
		private final Socket socket;
		// Service handling
		private final Map<String, Invocable<?>> serviceMap;
		// Protocol and Format Processor
		private AbstractProtocolProcessor protocolProcessor;
		// Message format processing
		private final IFormatProcessor formatProcessor;

		/**
		 * Initializes a newly created ClientHandlerRunnable object with an accepted socket connection.
		 */
		public ClientHandlerRunnable(Socket s) {
			this.socket = s;
			// Constant reference used for service handling
			this.serviceMap = RpcSocketServer.this.serviceMap;
			// Constant reference used for message format processing 
			this.formatProcessor = RpcSocketServer.this.formatProcessor;
			
			// Create a dedicated protocol processor used for communication. 
			try {
				this.protocolProcessor = RpcSocketServer.this.protocolProcessorClass
						.getDeclaredConstructor(InputStream.class, OutputStream.class)
						.newInstance(socket.getInputStream(), socket.getOutputStream());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | IOException e) {
				this.protocolProcessor = null;
				e.printStackTrace();
				return;
			}
		}

		/**
		 * The mechanism of handling client request singly. 
		 * <p> 
		 * When ClientHandlerRunnable is used for creating a thread, 
		 * starting the thread causes the object's run method to be called in that separately executing thread. 
		 */
		@Override
		public void run() {
			try {
				// Confirm ready. 
				protocolProcessor.writeReady();
				
				while(!Thread.currentThread().isInterrupted()) {
					// Check input.
					if(!protocolProcessor.ready()) {
						System.out.println("Input buffer empty, sleep for 1000ms!");
						Thread.sleep(1000);
						continue;
					}
					// Fetch requested message.
					String received = protocolProcessor.readAndDecode(); 
					// Check if received message is exit request. 
					if(protocolProcessor.isExit(received)) {
						System.out.println("Client " + this.socket + " sends exit...");
						break;
					}
					// Decode the service name. 
					String requestMethod = formatProcessor.getMethod(received);
					// Decode for input parameter. 
					Object object = formatProcessor.decodeParam(received, paramClassMap.get(requestMethod));
					
					// Prepare request context (parameter and returned object). 
					InvocationContext<Object> requestContext = new InvocationContext<>();
					requestContext.param = object;
					
					// Invoke the designated service.
					Invocable<Object> designatedMethod = (Invocable<Object>) serviceMap.get(requestMethod);
					designatedMethod.invoke(requestContext);
					
					// Generate the reply with FormatProcessor. 
					String replyString =  formatProcessor.encode(received, null, requestContext.returnVal);
					// Encapsulate message according with protocol and send it. 
					protocolProcessor.write(replyString);
				}
			} catch (InterruptedException e) { 
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 

			// closing resources.
			try {
				protocolProcessor.writeExit();
				protocolProcessor.close();
				System.out.println("Closing current socket connection.");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}