package project.remote.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageDecode;
import project.remote.common.service.NetMessage;
import project.remote.server.service.ServerService;

public class ServerLocal {
	
	public static void main(String[] args) throws IOException {
		
		DataInputStream dis  = new DataInputStream(System.in);
		DataOutputStream dos = new DataOutputStream(System.out);
		ServerService serverService = new ServerService();
		
		
		String received;
		
		while (true) {
			
			try {

				// receive the answer from client
				received = dis.readUTF();
				
//				System.out.println("Received: " + received);
				if (received.equals("Exit")) {
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
			dis.close();
			dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
