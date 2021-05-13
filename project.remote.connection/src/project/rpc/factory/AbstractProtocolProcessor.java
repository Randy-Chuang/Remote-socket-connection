package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * Remove all the method arguments of IO controller. 
 * The read IO class shall be defined in extending class (Buffered RW or Object IO stream). 
 * - Required data members / member functions in Factory.Builder class (not yet created): 
 * 		- Class<? extends AbstractProtocolProcess> set with default value (Conventional protocol)
 * 		- Class<? implement IFormatProcessor> set with default JSON
 * 		- create() method call the corresponding constructor 
 * 		- getServer(Client)Builder called with required arguments ((IP), port) 
 */

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
	
	
	protected abstract boolean ready(BufferedReader reader);
	
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
