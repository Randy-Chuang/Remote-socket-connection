package project.remote.common.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class IOUtility {
	// Issues: method ready() doesn't guarantee the input buffer contain any new line character.
//	/**
//	 * Read a line of input (non-blocking method). 
//	 * @param reader 
//	 * @return a line of input with the line separator removed. 
//	 * @throws IOException 
//	 */
//	public static String readLineNonBlocking(BufferedReader reader) throws IOException {
//		if(reader.ready()) {
//			return reader.readLine();
//		}
//		return null;
//	}
	
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
			// Buffer clear before reusing it. 
			Arrays.fill(buffer, '\0');
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
	
	public static void write(DataOutputStream dos, String tosend) throws IOException {
		// write byte array to DataOutputStream
		dos.write(tosend.getBytes(), 0, tosend.length());
		dos.flush();
	}
}
