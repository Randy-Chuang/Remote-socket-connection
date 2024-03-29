package project.remote.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.common.service.LspBaseProtocol;

public class ClientLocal {
	/*
	 * Communicate with server process via StdIn / StdOut.
	 */
	public static void main(String[] args) throws Exception {
//		String serverExecCommand = "/home/randy/Downloads/TestProgram/server";
//		String serverExecCommand = "java -jar /home/randy/Desktop/temp/ServerLocal.jar";
		// Check for user input executable file. 
		String serverExecCommand = "";
		if(args.length > 0) {
			File file = new File(args[0]);
			String path = file.getAbsolutePath();
			if(path.substring(path.lastIndexOf('.') + 1).trim().equals("jar")) {
				serverExecCommand = "java -jar " + path;
			}
			else if(file.canExecute()) {
				serverExecCommand = path;
			}
			else {
				throw new Exception("main: Unrecognized file.");
			}
		}
		else {
			throw new Exception("main: too few arguments for main method.");
		}
		// Open a process by executing an executable
		Process serverProcess = Runtime.getRuntime().exec(serverExecCommand);
		
		// Encapsulate StdIn / StdOut for Process. 
		BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(serverProcess.getOutputStream()));
		
		
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
