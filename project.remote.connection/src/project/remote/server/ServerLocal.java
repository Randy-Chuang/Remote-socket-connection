package project.remote.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageDecode;
import project.remote.common.service.NetMessage;
import project.remote.server.service.ServerService;

public class ServerLocal {
	private static final String INPUT_DELIMITER_STRING = "\r\n";
	
	public static void main(String[] args) throws IOException {
		
		Scanner scanner = new Scanner(System.in);
		scanner.useDelimiter(INPUT_DELIMITER_STRING);
//		DataInputStream dis  = new DataInputStream(System.in);
		DataOutputStream dos = new DataOutputStream(System.out);
		ServerService serverService = new ServerService();
		
		
		String received;
		writeToDataOutputStream(dos, "OK\n");
		while (true) {
			
			try {
				// receive the answer from client
				received = readFromDataInputStream(scanner);
				
				if(received == null || received.isBlank()) {
					break;
				}

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
					writeToDataOutputStream(dos, NetMessage.netMessageEncode(jsonReply));
//					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;

				case "getSystemInfo":
					jsonReply = serverService.getServerSystemInfo(jsonRequest);
					writeToDataOutputStream(dos, NetMessage.netMessageEncode(jsonReply));
//					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;
					
				case "square":
					jsonReply = serverService.getServerSquare(jsonRequest);
					writeToDataOutputStream(dos, NetMessage.netMessageEncode(jsonReply));
//					dos.writeUTF(NetMessage.netMessageEncode(jsonReply));
					break;

				default:
					writeToDataOutputStream(dos, "Invalid Input");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// closing resources
			scanner.close();
			dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void writeToDataOutputStream(DataOutputStream dos, String tosend) throws IOException {
		dos.write(tosend.getBytes());
		dos.flush();
	}
	
	public static String readFromDataInputStream(Scanner scanner) throws IOException {
		String inputString = "";
		int lineCount = 3; 
		while(scanner.hasNext() && lineCount-- != 0) {
			inputString += scanner.next() + INPUT_DELIMITER_STRING;
			
		}
		return inputString;
//		byte[] array = new byte[bufferSize];
//		int readLength = dis.read(array);
//		if(readLength != -1) {
//			return new String(array);
//		}
	}
}
