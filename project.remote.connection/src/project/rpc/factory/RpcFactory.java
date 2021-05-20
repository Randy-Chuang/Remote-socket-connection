package project.rpc.factory;

import java.io.IOException;

import project.rpc.factory.format.JsonFormatProcessor;
import project.rpc.factory.format.XmlFormatProcessor;

/*
 * TODO:
 * 0. writing doc: lost the link to external class reference (com.google.gson.JsonObject)
 * 0.5. writing user guide: mention that the system service should be thread safe (or using static method)
 * 
 * --------------------
 * 10. Supporting multiple input parameters. 
 * 11. Complete mechanism to handle / resolve any possible scenario occurred in practice. 
 * 		Mechanism: Time out, 
 * 		Scenario: unexpected disconnection (without receiving exit message), wrong format (currently, throw exception and exit)
 * 
 * - Current class hierarchy is showing below:
 * 		Factory -> RpcSocketServer/Client -> ProtocolProcessor (read, write, encode/decode NetMessage)
 * 										  -> FormatProcessor (encode/decode message with specific format e.g. JSON or XML)
 * 										  -> (Server) RequestHandler (Invoke corresponding service and return with an object)
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
 *          <li>XML Format</li>
 *      </ul>
 * </ul>
 * 
 * @version 1.2
 *
 */
public class RpcFactory {
	/**
	 * Get a new instance of RpcFactory. 
	 * @return a new instance of RpcFactory. 
	 */
	public static RpcFactory getInstance() {
		return new RpcFactory();
	}

	/**
	 * Get an instance of <b>IRpcServer</b> with socket connection and JSON message format as default.  
	 * @param portNumber Port number which provides the server services. 
	 * @throws IOException if fail to create the ServerSocket due to some reasons (i.e. occupied port).
	 */
	public IRpcServer getSocketServer(int portNumber) throws IOException {
		return getJsonSocketServer(portNumber);
	}

	/**
	 * Get an instance of <b>IRpcClient</b> with socket connection and JSON message format as default.  
	 * @param hostAddr Host address of the server, it could be either IP or URL address. 
	 * @param portNumber Corresponding port number that provides the server services. 
	 */
	public IRpcClient getSocketClient(String hostAddr, int portNumber) {
		return getJsonSocketClient(hostAddr, portNumber);
	}

	/**
	 * Get an instance of <b>IRpcServer</b> with socket connection and JSON message format.  
	 * @param portNumber Port number which provides the server services. 
	 * @throws IOException if fail to create the ServerSocket due to some reasons (i.e. occupied port).
	 */
	public IRpcServer getJsonSocketServer(int portNumber) throws IOException {
		return new RpcSocketServer(portNumber).setFormatProcessor(JsonFormatProcessor.class);
	}
	
	/**
	 * Get an instance of <b>IRpcServer</b> with socket connection and XML message format.  
	 * @param portNumber Port number which provides the server services. 
	 * @throws IOException if fail to create the ServerSocket due to some reasons (i.e. occupied port).
	 */
	public IRpcServer getXmlSocketServer(int portNumber) throws IOException {
		return new RpcSocketServer(portNumber).setFormatProcessor(XmlFormatProcessor.class);
	}

	/**
	 * Get an instance of <b>IRpcClient</b> with socket connection and JSON message format.  
	 * @param hostAddr Host address of the server, it could be either IP or URL address. 
	 * @param portNumber Corresponding port number that provides the server services. 
	 */
	public IRpcClient getJsonSocketClient(String hostAddr, int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber).setFormatProcessor(JsonFormatProcessor.class);
	}
	
	/**
	 * Get an instance of <b>IRpcClient</b> with socket connection and XML message format.  
	 * @param hostAddr Host address of the server, it could be either IP or URL address. 
	 * @param portNumber Corresponding port number that provides the server services. 
	 */
	public IRpcClient getXmlSocketClient(String hostAddr, int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber).setFormatProcessor(XmlFormatProcessor.class);
	}

}
