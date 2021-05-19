package project.rpc.factory.protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.NetMessage;

public class DefaultProtocolProcessor extends AbstractProtocolProcessor {
	private BufferedReader reader;
	private BufferedWriter writer;

	public DefaultProtocolProcessor(InputStream inStream, OutputStream outStream) {
		super("Exit", "OK");
		// Encapsulate input and output stream.
		this.reader = new BufferedReader(new InputStreamReader(inStream));
		this.writer = new BufferedWriter(new OutputStreamWriter(outStream));
	}
	
	@Override
	public boolean ready() {
		try {
			return reader.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String readLine() {
		try {
			if(ready()) {
				return reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String decode(String header) {
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
	public void writeReady() {	
		String tosend = encode(getReadyString());
		send(tosend);
	}

	@Override
	public void waitReadyBlocking() {
		System.out.print("Protocol Processor waits for ready message... ");
		String received;
		do {
			received = readResponseBlocking();
		} while (!getReadyString().equals(received));
		System.out.println(received);
		System.out.flush();
	}

	@Override
	public void write(Object object) {
		String tosend = encode(object);
		send(tosend);
	}
	/*
	 * Wait for formatted response defined by protocol.
	 */
	@Override
	public String readResponseBlocking() {
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
	protected void send(String tosend) {
		try {
			writer.write(tosend, 0, tosend.length());
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeExit() {
		String tosend = encode(getExitString());
		send(tosend);
	}

	@Override
	public void close() {	
		try {
			if(reader != null) {
				reader.close();
				reader = null;
			}
			if(writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isclosed() {
		return reader == null && writer == null;
	}
	
}
