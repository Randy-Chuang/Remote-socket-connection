package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public abstract class AbstractProtocolProcessor{
	private String okString;
	private String exitString;
	
	protected AbstractProtocolProcessor(String exitString, String okString) {
		this.exitString = exitString;
		this.okString = okString;
	}
	
	protected boolean isExit(String message) {
		return this.exitString.equals(message);
	}
	protected void setExitString(String exitString) {
		this.exitString = exitString;
	}
	protected String getExitString() {
		return exitString;
	}
	
	protected boolean isOk(String message) {
		return this.okString.equals(message);
	}
	protected void setOkString(String okString) {
		this.okString = okString;
	}
	protected String getOkString() {
		return okString;
	}
	
	/*
	 * Message you are going to decode. Return null or empty to skip processing.
	 */
	protected abstract String read(BufferedReader reader);
	/*
	 * Decode message / header, BufferedReader is used for reading further input for decode. 
	 */
	protected abstract String decode(BufferedReader reader, String header);
	
	protected abstract String encode(Object object);
	
	
	protected abstract void writeOk(BufferedWriter writer);
	
	protected abstract void waitOkBlocking(BufferedReader reader);
	
	protected abstract void writeExit(BufferedWriter writer);
	
	protected abstract void write(BufferedWriter writer, String tosend);
	
	protected abstract String readResponseBlocking(BufferedReader reader); 
}
