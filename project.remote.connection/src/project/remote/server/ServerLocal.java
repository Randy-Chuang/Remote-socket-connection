package project.remote.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageDecode;
import project.remote.common.service.NetMessage;
import project.remote.server.service.ServerService;

public class ServerLocal {
	/*
	 * Provides server services and communicates via StdIn / StdOut.
	 */
	public static void main(String[] args) {
		// Encapsulate StdIn / StdOut.
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		ServerService serverService = new ServerService();
		
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
					break;
				}
				
				// Decode for header and get the length of request.
				int length = NetMessage.decodeHeader(received);
				// skip a line. 
				reader.readLine();
				// fetch requested message with length.
				received = IOUtility.read(reader, length);
				
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
