package project.rpc.factory.protocol;

public abstract class AbstractProtocolProcessor{
	private String exitString, readyString;
	
	public AbstractProtocolProcessor(String exitString, String readyString) {
		this.exitString = exitString;
		this.readyString = readyString;
	}
	
	public boolean isExit(String message) {
		return this.exitString.equals(message);
	}
	public void setExitString(String exitString) {
		this.exitString = exitString;
	}
	public String getExitString() {
		return exitString;
	}
	
	public boolean isReady(String message) {
		return this.readyString.equals(message);
	}
	public void setReadyString(String readyString) {
		this.readyString = readyString;
	}
	public String getReadyString() {
		return readyString;
	}
	
	
	public abstract boolean ready();
	
	/*
	 * Message you are going to decode. Return null or empty to skip processing.
	 */
	public abstract String read();
	/*
	 * Decode message / header, BufferedReader is used for reading further input for decode. 
	 */
	public abstract String decode(String header);
	
	public abstract String encode(Object object);
	
	
	public abstract void writeReady();
	
	public abstract void waitReadyBlocking();
	
	public abstract void writeExit();
	
	public abstract void write(String tosend);
	
	public abstract String readResponseBlocking(); 
	
	public abstract void close();
	
	public abstract boolean isclosed();
}
