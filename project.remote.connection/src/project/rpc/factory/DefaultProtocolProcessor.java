package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.NetMessage;

public class DefaultProtocolProcessor extends AbstractProtocolProcessor{

	protected DefaultProtocolProcessor() {
		super("Exit", "OK");
	}
	
	@Override
	protected boolean ready(BufferedReader reader) {
		try {
			return reader.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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
		if(object instanceof JsonObject) {
			try {
				return NetMessage.netMessageEncode((JsonObject)object);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		else if(object instanceof String) {
			return NetMessage.netMessageEncode((String)object);
		}
		else {
			return null;
		}
	}

	@Override
	protected void writeOk(BufferedWriter writer) {	
		String tosend = encode(getOkString());
		write(writer, tosend);
	}

	@Override
	protected void waitOkBlocking(BufferedReader reader) {
		String received;
		do {
			received = readResponseBlocking(reader);
		} while (!getOkString().equals(received));
		System.out.println("Received: " + received);
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
		} catch (Exception e) {
			e.printStackTrace();
			received = null;
		}
		return received;
	}

	@Override
	protected void writeExit(BufferedWriter writer) {
		String tosend = encode(getExitString());
		write(writer, tosend);
	}
}
