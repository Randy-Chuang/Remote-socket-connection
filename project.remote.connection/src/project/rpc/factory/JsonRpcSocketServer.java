package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageDecode;

public class JsonRpcSocketServer implements IJsonRpcServer {
	private final ServerSocket serverSocket;
	private final Map<String, Invocable> serviceMap;
	private AbstractProtocolProcessor processor;
	private Thread serverThread;
	
	public JsonRpcSocketServer(final int portNumber) throws IOException {
		this.serverSocket = new ServerSocket(portNumber);
		this.serviceMap = new TreeMap<String, Invocable>();
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
							Runnable runnable = new ClientHandlerRunnable(s, serviceMap, processor);
							Thread t = new Thread(runnable);
							t.start();
						} catch (IOException e) {
							s.close();
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		serverThread.start();
	}

	@Override
	public void stop() {
		// Close ServerSocket in order to terminate the blocking state of it. 
		System.out.println("Closing server.");
		try {
			serverSocket.close();
			System.out.println("ServerSocket closed.");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
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
	public void addRequestHandler(String name, Invocable r) {
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
		private final Map<String, Invocable> serviceMap;
		// Protocol message handling
		private final AbstractProtocolProcessor protocolProcessor;

		// Constructor
		public ClientHandlerRunnable(Socket s, Map<String, Invocable> serviceMap, AbstractProtocolProcessor protocolProcessor) throws IOException {
			this.socket = s;
			// Input and output buffer from Socket.
			this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			
			this.serviceMap = serviceMap;
			this.protocolProcessor = protocolProcessor;
		}

		@Override
		public void run() {
			try {
				// Confirming ready.
				protocolProcessor.writeOk(writer);;
				
				while(true) {
					// check input with ProtocolProcessor
					String received = protocolProcessor.read(reader);
					if(received == null || received.isBlank()) {
						System.out.println("Input buffer empty, sleep for 1000ms!");
						Thread.sleep(1000);					
						continue;
					}
					else if(protocolProcessor.isExit(received)) {
						System.out.println("Client " + this.socket + " sends exit...");
						break;
					}
					
					// fetch requested message from ProtocolProcessor.
					received = processor.decode(reader, received);
					
					JsonObject jsonRequest = MessageDecode.getJsonObject(received);
					String requestMethod = MessageDecode.getMethod(jsonRequest);
					
					InvocationContext requestContext = new InvocationContext();
					requestContext.rawRequest = new String(received);
					// Invoke the designated method
					Invocable designatedMethod = serviceMap.get(requestMethod);
					designatedMethod.invoke(requestContext);
					String tosend = protocolProcessor.encode(requestContext.returnVal);

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
				reader.close();
				writer.close();
				System.out.println("Closing this connection.");
				socket.close();
				System.out.println("Connection closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}