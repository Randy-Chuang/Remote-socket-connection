package project.rpc.factory;

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
	
	
	protected abstract boolean ready();
	
	/*
	 * Message you are going to decode. Return null or empty to skip processing.
	 */
	protected abstract String read();
	/*
	 * Decode message / header, BufferedReader is used for reading further input for decode. 
	 */
	protected abstract String decode(String header);
	
	protected abstract String encode(Object object);
	
	
	protected abstract void writeOk();
	
	protected abstract void waitOkBlocking();
	
	protected abstract void writeExit();
	
	protected abstract void write(String tosend);
	
	protected abstract String readResponseBlocking(); 
	
	protected abstract void close();
	
	protected abstract boolean isclosed();
}
