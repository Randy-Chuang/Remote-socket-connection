package project.rpc.factory;

import java.io.IOException;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;
import project.remote.server.service.SystemService;

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
	
	public IRpcClient getSocketClient(String hostAddr , int portNumber) {
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
	
	public IRpcClient getJsonSocketClient(String hostAddr , int portNumber) {
		return new RpcSocketClient(hostAddr, portNumber).setFormatProcessor(JsonFormatProcessor.class);
	}
	
	
	public static void main(String[] args) {
		String hostAddrss = "127.0.0.1";
		int portNumber = 5056;
		
		/// server code
		SystemService systemService = new SystemService();
		RpcFactory factory = RpcFactory.getInstance();
		IRpcServer server = factory.getJsonSocketServer(portNumber);
		server.addRequestHandler("getDate", (ctx) -> {
			ctx.returnVal = systemService.getDate();
		});
		server.addRequestHandler("getSystemInfo", (ctx) -> {
			ctx.returnVal = systemService.getSystemInfo();
		});
		server.<Double>addRequestHandler("square", (ctx) -> {
			ctx.returnVal = systemService.square(ctx.param);
		});
		server.addParameterClass("getDate", null);
		server.addParameterClass("getSystemInfo", null);
		server.addParameterClass("square", Double.class);
		server.start();
		
		/// client code
		IRpcClient client = factory.getJsonSocketClient(hostAddrss, portNumber);
		client.addReturnedClass("getDate", DateInfo.class);
		client.addReturnedClass("getSystemInfo", SystemInfo.class);
		client.addReturnedClass("square", Double.class);
		
		System.out.println("Client starts sending request----------------------");
		
		client.start();
		// invoke() would return the class object associated with the service
		client.invoke("getDate");
		client.invoke("square", 1.5);
		client.invoke("getSystemInfo");
		
		client.stop();
		
		System.out.println("Client closes socket connection ----------------------");
	
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		server.stop();
	}
}
