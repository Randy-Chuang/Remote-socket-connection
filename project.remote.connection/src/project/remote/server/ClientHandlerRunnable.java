package project.remote.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageDecode;
import project.remote.common.service.NetMessage;
import project.remote.server.service.ServerService;

public class ClientHandlerRunnable implements Runnable {
	private final Socket socket;
//	private final DataInputStream dis;
//	private final DataOutputStream dos;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	
	private final ServerService serverService;

	// Constructor
	public ClientHandlerRunnable(Socket s) throws IOException {
		this.socket = s;
		// obtaining input and out streams
//		this.dis = new DataInputStream(s.getInputStream());
//		this.dos = new DataOutputStream(s.getOutputStream());
		
		this.reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		
		this.serverService = new ServerService();
	}

	@Override
	public void run() {
		try {
			String received;
			// Confirming ready.
			String tosend = "OK";
			IOUtility.write(writer, tosend);
			
			while(true) {
				// check for buffer of input
				if(!reader.ready()) {
//					System.out.println("Input buffer empty, sleep for 1000ms!");
					Thread.sleep(1000);
					continue;
				}
				
				received = reader.readLine();
				if(received != null && received.isBlank()) {
					continue;
				}
				else if(received == null || received.equals("Exit")) {
					System.out.println("Client " + this.socket + " sends exit...");
					break;
				}
				
				// Decode for header and get the length of request.
				int length = NetMessage.decodeHeader(received);
				// skip a line. 
				reader.readLine();
				// fetch requested message with length.
				received = IOUtility.read(reader, length);
				System.out.println("got: " + received);
				JsonObject jsonRequest = MessageDecode.getJsonObject(received);
				String requestMethod = MessageDecode.getMethod(jsonRequest);
				JsonObject jsonReply = null;
				// Invoke the designated method
				switch (requestMethod) {
				case "getDate":
					jsonReply = serverService.getServerDate(jsonRequest);
					tosend = NetMessage.netMessageEncode(jsonReply);
					break;
				case "getSystemInfo":
					jsonReply = serverService.getServerSystemInfo(jsonRequest);
					tosend = NetMessage.netMessageEncode(jsonReply);
					break;
				case "square":
					jsonReply = serverService.getServerSquare(jsonRequest);
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