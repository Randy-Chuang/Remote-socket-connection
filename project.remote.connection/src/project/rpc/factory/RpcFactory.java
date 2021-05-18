package project.rpc.factory;

import java.io.IOException;

import project.rpc.factory.format.JsonFormatProcessor;

/*
 * TODO: 
 * 0. First-stage documentation writing. 
 * 1. XML format.
 * 		A. Request / Reply format: XML - XMLEncoder
 * 1.2. Currently, we use lambda expression to register our system service, and the system service is invoked by object methods (not static), is it safe to do so? 
 * 1.5. Responding with invalid request (wrong name of services)
 * 2. Invoking server service with user-defined Class (or even an array) would cause an exception while casting. 
 * 3. Manage your design of factory pattern with a clearer structure, current class hierarchy is showing below:
 * 		Factory -> JsonRpcServer/Client -> ProtocolProcessor (read, write, encode/decode NetMessage)
 * 										-> FormatProcessor (encode/decode message with specific format e.g. JSON or XML)
 * 										-> (Server) RequestHandler (Invoke corresponding service and return with an object)
 * 5. Restructure
 * 80. Confirming that accessing same object with multiple threads won't produce any problem. (race condition)
 * 90. Time out mechanism and unexpected disconnection (without receiving exit message) handling. 
 */

/**
 * Providing the design of <B>Factory Pattern</B> to help you build the remote connection (RPC) 
 * and configure the communication end point easily without dealing with the detail of background process and 
 * mechanism. 
 * <p>
 * Currently, it provides socket connection with:  
 * <ul> 
 *  <li>Protocol Processing: </li>
 *      <ul>
 *          <li>
 *              Default Protocol Processing: messages are encapsulated with a common header which designates the length 
 *              of the following message. 
 *          </li>
 *      </ul>
 *  <li>Format Processing: </li>
 *      <ul>
 *          <li>JSON Format (Default)</li>
 *      </ul>
 * </ul>
 * 
 * @version 1.0
 *
 */
public class RpcFactory {
	/**
	 * Get the instance of RpcFactory. 
	 */
	public static RpcFactory getInstance() {
		return new RpcFactory();
	}

	/**
	 * Get an instance of <b>IRpcServer</b> with socket connection and JSON message format in default.  
	 * @param portNumber Port number which provides the server services. 
	 */
	public IRpcServer getSocketServer(int portNumber) {
		return getJsonSocketServer(portNumber);
	}

	/**
	 * Get an instance of <b>IRpcClient</b> with socket connection and JSON message format in default.  
	 * @param hostAddr Host address of the server, it could be either IP or URL address. 
	 * @param portNumber Corresponding port number that provides the server services. 
	 */
	public IRpcClient getSocketClient(String hostAddr, int portNumber) {
		return getJsonSocketClient(hostAddr, portNumber);
	}

	/**
	 * Get an instance of <b>IRpcServer</b> with socket connection and JSON message format.  
	 * @param portNumber Port number which provides the server services. 
	 */
	public IRpcServer getJsonSocketServer(int portNumber) {
		try {
			return new RpcSocketServer(portNumber).setFormatProcessor(JsonFormatProcessor.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get an instance of <b>IRpcClient</b> with socket connection and JSON message format.  
	 * @param hostAddr Host address of the server, it could be either IP or URL address. 
	 * @param portNumber Corresponding port number that provides the server services. 
	 */
	public IRpcClient getJsonSocketClient(String hostAddr, int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber).setFormatProcessor(JsonFormatProcessor.class);
	}

}
