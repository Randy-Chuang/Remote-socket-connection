package project.remote.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.common.service.LspBaseProtocol;

/*
 * TODO:
 * 90. Mechanism: Unexpected disconnection from Server (time out and ??)
 */

public class Client {
	
	public static void main(String[] args) {
		final String serverIpAddress = "127.0.0.1"; // loop-back IP address. 
		int serverPort = 5056;
		if(args.length > 0) {
			serverPort = Integer.valueOf(args[0]).intValue();
		}
		System.out.println("Socket client would connect to localhost on port: " + serverPort);
		
		try {
			InetAddress ip = InetAddress.getByName(serverIpAddress);
			
			// establish the connection with server ip address and port
			Socket socket = new Socket(ip, serverPort);
			
			// Encapsulate StdIn / StdOut for Process. 
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			// Confirming ready.
			String received = IOUtility.waitForDesignatedInput(reader, "OK", false);
			System.out.println(received);

			// square method
			JsonObject jsonRequest = MessageEncode.encodeSquare(null, null);
			jsonRequest.addProperty(MessageField.PARAMETERS_OBJ_STRING, 2.2);
			String tosend = LspBaseProtocol.encode(jsonRequest, false);
			IOUtility.write(writer, tosend);
			waitForResponse(reader);

			// getSystemInfo method
			jsonRequest = MessageEncode.encodeSystemInfo(null, null);
			tosend = LspBaseProtocol.encode(jsonRequest, false);
			IOUtility.write(writer, tosend);
			waitForResponse(reader);

			// getDateInfo method
			jsonRequest = MessageEncode.encodeDateInfo(null, null);
			tosend = LspBaseProtocol.encode(jsonRequest, false);
			IOUtility.write(writer, tosend);
			waitForResponse(reader);

			tosend = "Exit";
			IOUtility.write(writer, tosend);

			// closing resources
			reader.close();
			writer.close();
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * Wait for formatted response defined by protocol.
	 */
	public static void waitForResponse(BufferedReader reader) throws Exception {
		String received = IOUtility.waitForNextLine(reader);
		
		// Decode for header and get the length of request.
		int length = LspBaseProtocol.decodeHeader(received);
		// skip a line. 
		reader.readLine();
		// fetch requested message with length.
		received = IOUtility.read(reader, length);
		
		System.out.println(received);
	}
}