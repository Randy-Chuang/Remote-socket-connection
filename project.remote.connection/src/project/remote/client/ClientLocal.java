package project.remote.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.common.service.NetMessage;

public class ClientLocal {
	

	/*
	 * Communicate with server process via StdIn / StdOut.
	 */
	public static void main(String[] args) throws IOException {
		// Open a process by executing an executable
//		Process serverProcess = Runtime.getRuntime().exec("java -jar /home/randy/Desktop/temp/ServerLocal.jar");
		Process serverProcess = Runtime.getRuntime().exec("/home/randy/Downloads/TestProgram/server");
		
		// obtaining input and out streams
		DataInputStream dis = new DataInputStream(serverProcess.getInputStream());
		DataOutputStream dos = new DataOutputStream(serverProcess.getOutputStream());
		
		printFromDataInputStream(dis);
		
		// square method
		JsonObject jsonRequest = MessageEncode.encodeSquare(null, null);
		jsonRequest.addProperty(MessageField.PARAMETERS_OBJ_STRING, 2.2);
		String tosend = NetMessage.netMessageEncode(jsonRequest);
		writeToDataOutputStream(dos, tosend);		
		printFromDataInputStream(dis);

		// getSystemInfo method
		jsonRequest = MessageEncode.encodeSystemInfo(null, null);
		tosend = NetMessage.netMessageEncode(jsonRequest);
		writeToDataOutputStream(dos, tosend);		
		printFromDataInputStream(dis);

		// getDateInfo method
		jsonRequest = MessageEncode.encodeDateInfo(null, null);
		tosend = NetMessage.netMessageEncode(jsonRequest);
		writeToDataOutputStream(dos, tosend);		
		printFromDataInputStream(dis);

		writeToDataOutputStream(dos, "Exit");		

		// closing resources
		dis.close();
		dos.close();
	}
	
	public static void writeToDataOutputStream(DataOutputStream dos, String tosend) throws IOException {
		// write byte array to DataOutputStream
		dos.write(tosend.getBytes(), 0, tosend.length());
		dos.flush();
	}
	
	private static final int bufferSize = 1024;
	
	public static void printFromDataInputStream(DataInputStream dis) throws IOException {
		byte[] array = new byte[bufferSize];
		// This method blocks until input data is available, EOF is detected, or an exception is thrown.
		int readLength = dis.read(array);
		if(readLength != -1) {
			String string = new String(array);
			string = string.trim();
			System.out.println(string);
		}
	}
	
}
