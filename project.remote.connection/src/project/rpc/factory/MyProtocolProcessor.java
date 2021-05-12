package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.NetMessage;

public class MyProtocolProcessor extends AbstractProtocolProcessor{

	protected MyProtocolProcessor() {
		super("Exit", "OK");
	}

	@Override
	protected String read(BufferedReader reader) {
		try {
			if(reader.ready()) {
				return reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String decode(BufferedReader reader, String header) {
		String received;
		try {
			// Decode for header and get the length of request.
			int length = NetMessage.decodeHeader(header);
			// skip a line. 
			reader.readLine();
			// fetch requested message with length.
			received = IOUtility.read(reader, length);
		} catch (Exception e) {
			received = null;
			e.printStackTrace();
		}
		return received;
	}

	@Override
	protected String encode(Object object) {
		try {
			return NetMessage.netMessageEncode((JsonObject)object);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void writeOk(BufferedWriter writer) {
		write(writer, getOkString());		
	}

	@Override
	protected void waitOkBlocking(BufferedReader reader) {
		try {
			String received = IOUtility.waitForDesignatedInput(reader, getOkString(), false);
			System.out.println("Receive: " + received);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void write(BufferedWriter writer, String tosend) {
		try {
			writer.write(tosend, 0, tosend.length());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * Wait for formatted response defined by protocol.
	 */
	@Override
	protected String readResponseBlocking(BufferedReader reader) {
		String received;
		try {
			received = IOUtility.waitForNextLine(reader);
			// Decode for header and get the length of request.
			int length = NetMessage.decodeHeader(received);
			// skip a line.
			reader.readLine();
			// fetch requested message with length.
			received = IOUtility.read(reader, length);
			
			System.out.println(received);
			
		} catch (Exception e) {
			e.printStackTrace();
			received = null;
		}
		
		return received;
	}

	@Override
	protected void writeExit(BufferedWriter writer) {
		write(writer, getExitString());
	}
}
