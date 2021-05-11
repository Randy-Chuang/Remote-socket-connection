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
import project.remote.common.service.NetMessage;

public class JsonRpcSocketServer implements IJsonRpcServer {
	private final ServerSocket serverSocket;
	private final Map<String, Invocable> serviceMap;
	private ProtocolProcessor processor;
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
				while (true) {
					Socket s = null;
					try {
						// socket object to receive incoming client requests
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
		serverThread.interrupt();
		try {
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public void addProtocolProcessor(ProtocolProcessor processor) {
		this.processor = processor;
	}
	
	
	private class ClientHandlerRunnable implements Runnable {
		private final Socket socket;
		private final BufferedReader reader;
		private final BufferedWriter writer;
		private final Map<String, Invocable> serviceMap;
		private final ProtocolProcessor protocolProcessor;

		// Constructor
		public ClientHandlerRunnable(Socket s, Map<String, Invocable> serviceMap, ProtocolProcessor protocolProcessor) throws IOException {
			this.socket = s;
			// obtaining input and out buffer
			this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			this.writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			this.serviceMap = serviceMap;
			this.protocolProcessor = protocolProcessor;
		}

		@Override
		public void run() {
			try {
				String received;
				// Confirming ready.
				String tosend = "OK";
//				IOUtility.write(writer, tosend);
				
				while(true) {
					// check input with ProtocolProcessor
					received = protocolProcessor.read(reader);
					if(received == null) {
						System.out.println("Input buffer empty, sleep for 1000ms!");
						Thread.sleep(1000);					
						continue;
					}
					else if(received.isBlank()) {
						continue;
					}
					else if(protocolProcessor.isExit(received)) {
						System.out.println("Client " + this.socket + " sends exit...");
						break;
					}
					
					// fetch requested message from ProtocolProcessor
					received = processor.decode(reader, received);
					
					JsonObject jsonRequest = MessageDecode.getJsonObject(received);
					String requestMethod = MessageDecode.getMethod(jsonRequest);
					JsonObject jsonReply = null;
					// Invoke the designated method
					switch (requestMethod) {
					case "getDate":
//						jsonReply = serverService.getServerDate(jsonRequest);
						tosend = NetMessage.netMessageEncode(jsonReply);
						break;
					case "getSystemInfo":
//						jsonReply = serverService.getServerSystemInfo(jsonRequest);
						tosend = NetMessage.netMessageEncode(jsonReply);
						break;
					case "square":
//						jsonReply = serverService.getServerSquare(jsonRequest);
						tosend = NetMessage.netMessageEncode(jsonReply);
						break;
					default:
						tosend = "Invalid Input";
						break;
					}
					// write message to output.
					IOUtility.write(writer, tosend);
				}
				
			} catch (IOException | InterruptedException e) {
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