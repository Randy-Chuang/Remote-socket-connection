package project.rpc.factory;

import java.io.IOException;

/*
 * TODO: 
 * 0. First-stage documentation writing. 
 * 1. XML format.
 * 		A. Request / Reply format: XML - XMLEncoder
 * 
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
public class RpcFactory {
	/**
	 * Test for <B>javadoc</B>.
	 * 
	 * @see #getJsonSocketServer(int)
	 * 
	 * @return
	 */
	public static RpcFactory getInstance() {
		return new RpcFactory();
	}

	/**
	 * @param portNumber
	 * @return
	 */
	public IRpcServer getSocketServer(int portNumber) {
		try {
			return new RpcSocketServer(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public IRpcClient getSocketClient(String hostAddr, int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber);
	}

	public IRpcServer getJsonSocketServer(int portNumber) {
		try {
			return new RpcSocketServer(portNumber).setFormatProcessor(JsonFormatProcessor.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public IRpcClient getJsonSocketClient(String hostAddr, int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber).setFormatProcessor(JsonFormatProcessor.class);
	}

}
