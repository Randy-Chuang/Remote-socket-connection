package project.remote.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.gson.JsonObject;

import project.remote.common.service.NetMessage;
import project.remote.common.service.MessageDecode;
import project.remote.server.service.ServerService;

public class ClientHandlerRunnable implements Runnable {
	private final Socket s;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	
	private final ServerService serverService;

	// Constructor
	public ClientHandlerRunnable(Socket s) throws IOException {
		this.s = s;
		// obtaining input and out streams
		this.dis = new DataInputStream(s.getInputStream());
		this.dos = new DataOutputStream(s.getOutputStream());
		
		this.serverService = new ServerService();
	}

	@Override
	public void run() {
		String received;
		String toreturn;
		
		while (true) {
			try {

				// Ask user what he wants
//				dos.writeUTF("Services: getDate, getSystemInfo, square");

				// receive the answer from client
				received = dis.readUTF();
				
				System.out.println("Received: " + received);
				if (received.equals("Exit")) {
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}
				
				JsonObject jsonRequest = null;
				String requestMethod = "";
				try {
					jsonRequest = NetMessage.netMessageDecode(received);
					requestMethod = MessageDecode.getMethod(jsonRequest);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				// write on output stream based on the
				// answer from the client
				JsonObject jsonReply = null;
				switch (requestMethod) {

				case "getDate":
					jsonReply = serverService.getServerDate(jsonRequest);
					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;

				case "getSystemInfo":
					jsonReply = serverService.getServerSystemInfo(jsonRequest);
					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;
					
				case "square":
					jsonReply = serverService.getServerSquare(jsonRequest);
					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;

				default:
					dos.writeUTF("Invalid input");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// closing resources
			this.dis.close();
			this.dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
}