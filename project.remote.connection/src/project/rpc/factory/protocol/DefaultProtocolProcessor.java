package project.rpc.factory.protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.JsonObject;

import project.remote.common.service.LspBaseProtocol;

/**
 * Protocol processor that works with the base protocol of Microsoft LSP specification to 
 * send/ fetch message over InputStream and OutputStream. 
 *
 * @see <a href="https://microsoft.github.io/language-server-protocol/specifications/specification-current/#baseProtocol">Microsoft LSP - Base protocol</a>
 */
public class DefaultProtocolProcessor extends AbstractProtocolProcessor {
	// the actual used I/O object. 
	private BufferedReader reader;
	private BufferedWriter writer;
	
	/**
	 * Initialize protocol processor with input and output stream. 
	 * @param inStream input for fetching incoming message. 
	 * @param outStream output for sending message. 
	 */
	public DefaultProtocolProcessor(InputStream inStream, OutputStream outStream) {
		super("Exit", "OK");
		// Encapsulate input and output stream.
		this.reader = new BufferedReader(new InputStreamReader(inStream));
		this.writer = new BufferedWriter(new OutputStreamWriter(outStream));
	}
	
	/**
	 * Check input is ready or not (non-blocking method).   
	 * @return true if there is something to be received; false otherwise. 
	 */
	@Override
	public boolean ready() {
		try {
			return reader.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Simply send the string through the output. 
	 * @param tosend the string to be sent to the output. 
	 */
	@Override
	protected void send(String tosend) {
		try {
			BufferedIOUtility.write(writer, tosend);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Decode protocol header and fetch/decode for further input according to protocol. 
	 * @param header the protocol header which encapsulates with info about network message. 
	 * @return 
	 */
	@Override
	protected String decode(String header) {
		String received;
		try {
			// Decode for header and get the length of request.
			int length = LspBaseProtocol.decodeHeader(header);
			// skip a line. 
			BufferedIOUtility.readLineBlocking(reader);
			// fetch requested message with length.
			received = BufferedIOUtility.read(reader, length);
		} catch (Exception e) {
			received = null;
			e.printStackTrace();
		}
		return received;
	}

	/**
	 * Encode object / message according to protocol. 
	 * @param object the content to be encapsulated in protocol communication. 
	 * @return the protocol encapsulated message.  
	 */
	@Override
	protected String encode(Object object) {
		if(object instanceof JsonObject) {
			return LspBaseProtocol.encode((JsonObject)object, false);
		}
		else if(object instanceof String) {
			return LspBaseProtocol.encode((String)object);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Read the protocol formatted message from input and decode for inner message in return. 
	 * @return the inner message encapsulated according protocol.
	 */
	@Override
	public String readAndDecode() {
		String received;
		try {
			received = BufferedIOUtility.waitForNextLine(reader);
			received = decode(received);
		} catch (Exception e) {
			e.printStackTrace();
			received = null;
		}
		return received;
	}

	/**
	 * Write ready message. 
	 */
	@Override
	public void writeReady() {	
		String tosend = encode(getReadyString());
		send(tosend);
	}

	/**
	 * 	Wait until ready message received. 
	 */
	@Override
	public void waitReadyBlocking() {
		System.out.print("Protocol Processor waits for ready message... ");
		String received;
		// Scanning for ready message.
		do {
			received = readAndDecode();
		} while (!getReadyString().equals(received));
		System.out.println(received);
		System.out.flush();
	}

	/**
	 * Write exit message. 
	 */
	@Override
	public void writeExit() {
		String tosend = encode(getExitString());
		send(tosend);
	}
	
	/**
	 * Write the given object / message. 
	 * @param object the content to be sent to the output (the actual Class of object should override <b>toString()</b> method). 
	 */
	@Override
	public void write(Object object) {
		String tosend = encode(object);
		send(tosend);
	}

	/**
	 * Close related resources (I/O) to protocol processor. 
	 */
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

	/**
	 * Check if protocol processor is closed. 
	 * @return true if protocol processor is closed; false otherwise. 
	 */
	@Override
	public boolean isclosed() {
		return reader == null && writer == null;
	}
	
	/**
	 * Performing IO processing with BufferedIO specifically. 
	 */
	private static class BufferedIOUtility { 
		/**
		 * Read a line of input (blocking method). 
		 * @param reader
		 * @return a line of input with the line separator removed. 
		 * @throws IOException
		 */
		public static String readLineBlocking(BufferedReader reader) throws IOException {
			return reader.readLine();
		}
		
		/**
		 * Wait for next line for BufferedReader. 
		 * @param reader
		 * @return
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public static String waitForNextLine(BufferedReader reader) throws IOException, InterruptedException {
			String text = "";
			// Get the next non-blank input.
			do {
				// Block until buffer ready for reading. 
				while(!reader.ready()) {
					Thread.sleep(10);
				}
				text = reader.readLine();
			}while(text.isBlank());
			
			return text;
		}
		
		public static String waitForDesignatedInput(BufferedReader reader, final String designated, final boolean caseSensitive) throws IOException, InterruptedException {
			int matchPos = -1;
			String string = new String(designated);
			if(!caseSensitive) {
				string = string.toLowerCase();
			}
			
			while(matchPos < string.length() - 1) {
				// Block until buffer ready for reading. 
				while(!reader.ready()) {
					Thread.sleep(10);
				}
				
				char c = (char) reader.read();
				c = (!caseSensitive) ? Character.toLowerCase(c) : c;
				
				if(c == string.charAt(matchPos + 1)) {
					matchPos++;
				}
				else {
					matchPos = -1;
					System.err.println("unmatched: " + c);
				}
			}
			return string;
		}

		private static final int bufferSize = 1024; 
		public static String read(BufferedReader reader, int length) throws IOException {
			char[] buffer = new char[bufferSize];
			int ret;
			String received = "";
			while(length > 0) {
				if(length >= bufferSize) {
					ret = reader.read(buffer, 0, bufferSize);
				}
				else {
					ret = reader.read(buffer, 0, length);
				}
				
				if(ret == -1) {
					throw new IOException("BufferedReader: shortage on characters in input buffer!");
				}
				length -= ret;
				String bufferString = new String(buffer).trim();
				received += bufferString;
			}
			return received;
		}
		
		
		public static void write(BufferedWriter bufferedWriter, String tosend) throws IOException {
			bufferedWriter.write(tosend, 0, tosend.length());
			bufferedWriter.flush();
		}
	}
	
}
