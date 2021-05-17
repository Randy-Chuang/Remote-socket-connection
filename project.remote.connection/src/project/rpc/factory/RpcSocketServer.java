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

/*
 * TODO: 
 * Issues: 
 * 1. Unable to determine the generic type of InvocationContext and Invocable when I retrieve it. 
 */

public class RpcSocketServer implements IRpcServer {
	private ServerSocket serverSocket;
	private final Map<String, Invocable<?>> serviceMap;
	// default value, shall be designated by user
	private IFormatProcessor formatProcessor = null;
	private Class<? extends IFormatProcessor> formatProcessorClass = JsonFormatProcessor.class;
	private final Map<String, Class<?>> paramClassMap = new TreeMap<>();
	private Class<? extends AbstractProtocolProcessor> protocolProcessorClass = DefaultProtocolProcessor.class;
	private Thread serverThread; 
	private ExecutorService threadPool; 
	
	public RpcSocketServer(final int portNumber) throws IOException {
		this.serverSocket = new ServerSocket(portNumber);
		this.serviceMap = new TreeMap<String, Invocable<?>>();
		this.threadPool = Executors.newFixedThreadPool(5);
	}
	
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
	
	@Override
	public void start() {
		
		try {
			formatProcessor = formatProcessorClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			formatProcessor = null;
			e.printStackTrace();
			return;
		}
		
		// Process server task with multithreading.
		serverThread = new Thread(new Runnable() {
			// Thread task for socket server.
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					Socket s = null;
					try {
						// socket object to receive incoming client requests
						// block until a new socket connection is established
						s = serverSocket.accept();
						System.out.println("A new client is connected : " + s);
						System.out.println("Assigning new thread for this client");

						// create a new Runnable, Thread used for client handling. 
						try {
							Runnable runnable = new ClientHandlerRunnable(s);
							threadPool.execute(runnable);
						} catch (Exception e) { //was IOException 
							s.close();
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		});
		serverThread.start();
	}
	
	@Override
	public void stop() {
		System.out.println("Closing server.");
		// set interrupt flag for all actively executing thread
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

	@Override
	public <T> void addRequestHandler(String name, Invocable <T> r) {
		// Check for duplication of services
		if(serviceMap.containsKey(name)) {
			System.err.println("Trying to override the existing service: " + name);
			return;
		}
		
//		Class<T> typeClass;
//		typeClass.get
//		System.err.println("add request param type: " + r.);
		
		// Add service
		serviceMap.put(name, r);
	}
	
	@Override
	public void addParameterClass(String service, Class<?> objectClass) {
		// Check for duplication of Class type
		if (paramClassMap.containsKey(service)) {
			System.err.println("Trying to override the existing Class type: " + service);
			return;
		}
		// Add Class type
		paramClassMap.put(service, objectClass);
	}
	
	private class ClientHandlerRunnable implements Runnable {
		private final Socket socket;
		// Service handling
		private final Map<String, Invocable<?>> serviceMap;
		// Protocol and Format Processor
		private AbstractProtocolProcessor protocolProcessor;
		private final IFormatProcessor formatProcessor;

		public ClientHandlerRunnable(Socket s) {
			this.socket = s;
			this.serviceMap = RpcSocketServer.this.serviceMap;
			this.formatProcessor = RpcSocketServer.this.formatProcessor;
			
			try {
				this.protocolProcessor = RpcSocketServer.this.protocolProcessorClass.getDeclaredConstructor(InputStream.class, OutputStream.class).newInstance(socket.getInputStream(), socket.getOutputStream());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | IOException e) {
				this.protocolProcessor = null;
				e.printStackTrace();
				return;
			}
		}

		@Override
		public void run() {
			try {
				// Confirming ready.
				protocolProcessor.writeOk();
				
				while(!Thread.currentThread().isInterrupted()) {
					// check input with ProtocolProcessor
					if(!protocolProcessor.ready()) {
						System.out.println("Input buffer empty, sleep for 1000ms!");
						Thread.sleep(1000);
						continue;
					}
					
					// fetch requested message from ProtocolProcessor.
					String received = protocolProcessor.readResponseBlocking();
					
					if(protocolProcessor.isExit(received)) {
						System.out.println("Client " + this.socket + " sends exit...");
						break;
					}
					
					String requestMethod = formatProcessor.getMethod(received);
					
					Object object = formatProcessor.decodeParam(received, paramClassMap.get(requestMethod));
						
					InvocationContext<Object> requestContext = new InvocationContext<>();
					requestContext.param = object;
					// Invoke the designated method
					Invocable<Object> designatedMethod = (Invocable<Object>) serviceMap.get(requestMethod);
					designatedMethod.invoke(requestContext);
					
					String replyString =  formatProcessor.encode(received, null, requestContext.returnVal);
					
					String tosend = protocolProcessor.encode(replyString);

					// write message to output.
					protocolProcessor.write(tosend);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 

			// closing resources
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