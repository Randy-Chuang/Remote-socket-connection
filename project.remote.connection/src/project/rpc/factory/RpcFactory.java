package project.rpc.factory;

import java.io.IOException;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;
import project.remote.server.service.SystemService;

/*
 * TODO: 
 * 0. JSON / XML formats and convention protocol are just like accessories taken from factory.
 * Before creating an instance of IRpcServer, wishing to set the properties of Builder first. 
 * 		A. Request / Reply format: JSON - GSON  / XML - XMLEncoder 		
 * 
 * 		C. User-defined invokable: request object and return object.	
 * 
 * 
 * 1.5. Responding with invalid request (wrong name of services)
 * 1.8. Dealing with unexpected disconnection (without receiving exit message)
 * 2. Invoking server service with user-defined Class (or even an array) would cause an exception while casting. 
 * 3. Manage your design of factory pattern with a clearer structure, current class hierarchy is showing below:
 * 		Factory -> JsonRpcServer/Client -> ProtocolProcessor (read, write, encode/decode NetMessage)
 * 										-> (Client) RequestGenerator (generate JsonRequest with given arguments of objects)
 * 										-> (Server) RequestHandler (Invoke corresponding service and output a replied JsonObject)
 * 4. User defined Protocol processing (seems to be a tedious job) 
 * -> how could I add different protocol to Factory and let user to modify it appropriately.  
 * 5. Restructure
 * 80. Confirming that accessing same object with multiple threads won't produce any problem.
 * 90. Time out mechanism. 
 */
public class RpcFactory {
	
	public static RpcFactory getInstance() {
		return new RpcFactory();
	}
	
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
		client.invoke("square", 1.1);
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
