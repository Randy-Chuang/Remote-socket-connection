package project.rpc.factory.protocol;

/**
 * The interface used for sending / fetching message according to specific protocol. 
 *
 */
public abstract class AbstractProtocolProcessor{
	// Strings used for claiming particular state or command. 
	private String exitString, readyString;
	
	/**
	 * Initialize common request / reply message used in protocol communication. 
	 * @param exitString the string used as a exiting signal. 
	 * @param readyString the string used to signal a ready state. 
	 */
	public AbstractProtocolProcessor(String exitString, String readyString) {
		this.exitString = exitString;
		this.readyString = readyString;
	}
	/**
	 * Check if given message is an exit message. 
	 * @param message the message to be examined. 
	 * @return true if the given message is an exit message; false otherwise. 
	 */
	public boolean isExit(String message) {
		return this.exitString.equals(message);
	}
	/**
	 * Setup exit message. 
	 * @param exitString the reference to exit message. 
	 */
	public void setExitString(String exitString) {
		this.exitString = exitString;
	}
	/**
	 * Get current exit message. 
	 * @return the exit message. 
	 */
	public String getExitString() {
		return exitString;
	}
	/**
	 * Check if given message is a ready message. 
	 * @param message the message to be examined. 
	 * @return true if the given message is a ready message; false otherwise. 
	 */
	public boolean isReady(String message) {
		return this.readyString.equals(message);
	}
	/**
	 * Setup ready message. 
	 * @param readyString the reference to ready message. 
	 */
	public void setReadyString(String readyString) {
		this.readyString = readyString;
	}
	/**
	 * Get current ready message. 
	 * @return the ready message. 
	 */
	public String getReadyString() {
		return readyString;
	}
	
	
	/**
	 * Check input is ready or not (non-blocking method).   
	 * @return true if there is something to be received; false otherwise. 
	 */
	public abstract boolean ready();
	/**
	 * Simply send the string through the output. 
	 * @param tosend the string to be sent to the output. 
	 */
	protected abstract void send(String tosend);
	/**
	 * Decode protocol header and fetch/decode for further input according to protocol. 
	 * @param header the protocol header which encapsulates with info about network message. 
	 * @return 
	 */
	protected abstract String decode(String header);
	/**
	 * Encode object / message according to protocol. 
	 * @param object the content to be encapsulated in protocol communication. 
	 * @return the protocol encapsulated message.  
	 */
	protected abstract String encode(Object object);
	
	/**
	 * Read the protocol formatted message from input and decode for inner message in return. 
	 * @return the inner message encapsulated according protocol.
	 */
	public abstract String readAndDecode(); 
	/**
	 * Write ready message to output. 
	 */
	public abstract void writeReady();
	/**
	 * 	Wait until ready message received from input. 
	 */
	public abstract void waitReadyBlocking();
	/**
	 * Write exit message to output. 
	 */
	public abstract void writeExit();
	/**
	 * Write the given object / message to output. 
	 * @param object the content to be sent to the output (the actual Class of object should override <b>toString()</b> method). 
	 */
	public abstract void write(Object object);
	
	/**
	 * Close related resources (I/O) to protocol processor. 
	 */
	public abstract void close();
	/**
	 * Check if protocol processor is closed. 
	 * @return true if protocol processor is closed; false otherwise. 
	 */
	public abstract boolean isclosed();
}
