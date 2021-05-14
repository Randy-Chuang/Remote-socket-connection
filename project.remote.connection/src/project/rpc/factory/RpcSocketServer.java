package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import project.remote.common.service.ServiceClass.DateInfo;

/*
 * TODO: 
 * Issues: 
 * 1. Unable to determine the generic type of InvocationContext and Invocable when I retrieve it. 
 * 
 */

public class RpcSocketServer implements IRpcServer {
	private final ServerSocket serverSocket;
	private final Map<String, Invocable<?>> serviceMap;
	private AbstractProtocolProcessor processor;
	// default value, shall be designated by user
	private Class<? extends IFormatProcessor> formatProcessorClass = JsonFormatProcessor.class;
	private Thread serverThread;
	private final ExecutorService threadPool; 
	
	public RpcSocketServer(final int portNumber) throws IOException {
		this.serverSocket = new ServerSocket(portNumber);
		this.serviceMap = new TreeMap<String, Invocable<?>>();
		this.threadPool = Executors.newFixedThreadPool(5);
	}
	
	public RpcSocketServer test1() {
		return null;
		
	}public RpcSocketServer test2() {
		return null;
		
	}
	
	@Override
	public void start() {
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
							Runnable runnable = new ClientHandlerRunnable(s, serviceMap, processor, formatProcessorClass);
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
		// Add service
		serviceMap.put(name, r);
	}
	
	@Override
	public void addProtocolProcessor(AbstractProtocolProcessor processor) {
		this.processor = processor;
	}
	
	
	private class ClientHandlerRunnable implements Runnable {
		private final Socket socket;
		private final BufferedReader reader;
		private final BufferedWriter writer;
		// Service handling
		private final Map<String, Invocable<?>> serviceMap;
		// Protocol message handling
		private final AbstractProtocolProcessor protocolProcessor;
		private final IFormatProcessor formatProcessor;

		// Constructor
		public ClientHandlerRunnable(Socket s, Map<String, Invocable<?>> serviceMap, AbstractProtocolProcessor protocolProcessor, Class<? extends IFormatProcessor> formatProcessClass) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			this.socket = s;
			// Input and output buffer from Socket.
			this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			
			this.serviceMap = serviceMap;
			this.protocolProcessor = protocolProcessor;
			
			this.formatProcessor = formatProcessClass.getDeclaredConstructor().newInstance();
		}

		@Override
		public void run() {
			try {
				// Confirming ready.
				protocolProcessor.writeOk(writer);
				
				while(!Thread.currentThread().isInterrupted()) {
					// check input with ProtocolProcessor
					if(!protocolProcessor.ready(reader)) {
						System.out.println("Input buffer empty, sleep for 1000ms!");
						Thread.sleep(1000);
						continue;
					}
					
					// fetch requested message from ProtocolProcessor.
					String received = protocolProcessor.readResponseBlocking(reader);
					
					if(protocolProcessor.isExit(received)) {
						System.out.println("Client " + this.socket + " sends exit...");
						break;
					}
					
					String requestMethod = formatProcessor.getMethod(received);
					
					Object object = formatProcessor.decodeParam(received);
					if(object != null)
						System.out.println("canonical name: " + object.getClass().getCanonicalName());
					InvocationContext<Object> requestContext = new InvocationContext<Object>();
					requestContext.param = object;
					// Invoke the designated method
					Invocable<Object> designatedMethod = (Invocable<Object>) serviceMap.get(requestMethod);
					designatedMethod.invoke(requestContext);
					
					String replyString =  formatProcessor.encode(received, null, requestContext.returnVal);
					
					String tosend = protocolProcessor.encode(replyString);

					// write message to output.
					protocolProcessor.write(writer, tosend);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} 

			// closing resources
			try {
				protocolProcessor.writeExit(writer);
				reader.close();
				writer.close();
				System.out.println("Closing current socket connection.");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}