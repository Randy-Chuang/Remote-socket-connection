package project.remote.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.common.service.NetMessage;

public class ClientLocal {

	public static void main(String[] args) throws IOException {
//		Process serverProcess = Runtime.getRuntime().exec("java -jar /home/randy/Desktop/temp/ServerLocal.jar");
		Process serverProcess = Runtime.getRuntime().exec("/home/randy/Downloads/TestProgram/server");
		
		// obtaining input and out streams
		DataInputStream dis = new DataInputStream(serverProcess.getInputStream());
		DataOutputStream dos = new DataOutputStream(serverProcess.getOutputStream());	
		
		// square method
		JsonObject jsonRequest = MessageEncode.encodeSquare(null, null);
		jsonRequest.addProperty(MessageField.PARAMETERS_OBJ_STRING, 2.2);
		String tosend = NetMessage.netMessageEncode(jsonRequest);
		dos.writeUTF(tosend);
		dos.flush();

		String received = dis.readUTF();
		System.out.println(received);

		// getSystemInfo method
		jsonRequest = MessageEncode.encodeSystemInfo(null, null);
		tosend = NetMessage.netMessageEncode(jsonRequest);
		dos.writeUTF(tosend);
		dos.flush();

		received = dis.readUTF();
		System.out.println(received);

		// getDateInfo method
		jsonRequest = MessageEncode.encodeDateInfo(null, null);
		tosend = NetMessage.netMessageEncode(jsonRequest);
		dos.writeUTF(tosend);
		dos.flush();

		received = dis.readUTF();
		System.out.println(received);

		dos.writeUTF("Exit");
		dos.flush();

		// closing resources
		dis.close();
		dos.close();
	}
}
