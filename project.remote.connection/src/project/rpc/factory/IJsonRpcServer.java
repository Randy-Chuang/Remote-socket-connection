package project.rpc.factory;

import java.io.BufferedReader;

public interface IJsonRpcServer {
    public void start();  // start server
    public void stop();  // stop server
    /*
     * Handler should assign returnVal if needed.
     */
    public void addRequestHandler(String name, Invocable r); 
    public void addProtocolProcessor(ProtocolProcessor processor);
    
    public abstract class ProtocolProcessor{
    	// TODO: OK message
    	
    	private String exitString;
    	
    	protected ProtocolProcessor(String exitString) {
			this.exitString = exitString;
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
    	
    	/*
    	 * Message you are going to decode. Return null or empty to skip processing.
    	 */
    	protected abstract String read(BufferedReader reader);
    	/*
    	 * Decode message / header, BufferedReader is used for reading further input for decode. 
    	 */
    	protected abstract String decode(BufferedReader reader, String header);
    	
    	protected abstract String encode(String message);
    }
}