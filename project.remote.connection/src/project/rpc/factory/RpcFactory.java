package project.rpc.factory;

import java.io.IOException;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;
import project.remote.server.service.SystemService;


/*
 * TODO: 
 * 0. JSON / XML formats and convention / object protocol are just like accessories taken from factory.
 * Before creating an instance of IRpcServer, wishing to set the properties of Builder first. 
 * Goal of calling: 
 * 		IRpcServer server = factory.getSocketServerBuilder().setJsonFormat().setConventionProtocol().create();
 * 									(getSocketServerBuilder() would set up some default setting)
 * 		=> 0.1. Complete protocol for convention and object IO stream. 
 * 		=> 0.2. Built-in server implementation should cooperate with different controls (Interface to control??)
 * 
 * 		A. Request / Reply format: JSON - GSON (custom name annotation) / XML - XMLEncoder / Object serialized format. 
 * 			
 * 
 * 		B. Communication protocol: Convention protocol (Content length header) / OOS OIS
 * 		
 * 
 * 		C. User-defined invokable: request object and return object.  		
 * 
 * 
 * 1.5. Responding with invalid request (wrong name of services)
 * 1.8. Dealing with unexpected disconnection (without receiving exit message)
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
	
	private RpcFactory() {}
	
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
	
	public IRpcClient getSocketClient(String hostAddr , int portNumber, AbstractProtocolProcessor processor) {
		return new RpcSocketClient(hostAddr, portNumber, processor);
	}
	
	
	public static void main(String[] args) {
		String hostAddrss = "127.0.0.1";
		int portNumber = 5056;
		
		/// server code
		SystemService systemService = new SystemService();
		RpcFactory factory = RpcFactory.getInstance();
		IRpcServer server = factory.getSocketServer(portNumber);
		server.<Object>addRequestHandler("getDate", (ctx) -> {
			ctx.returnVal = systemService.getDate();
		});
		
		server.<Object>addRequestHandler("getSystemInfo", (ctx) -> {
			ctx.returnVal = systemService.getSystemInfo();
		});
		server.<Double>addRequestHandler("square", (ctx) -> {
			ctx.returnVal = systemService.square(ctx.param);
		});
		
		server.<Object[]>addRequestHandler("stringConcat", (ctx) -> {
			ctx.returnVal = systemService.stringConcat((String)ctx.param[0], (String)ctx.param[1]);
		});
		
		DefaultProtocolProcessor protocolProcessor = new DefaultProtocolProcessor();
		server.addProtocolProcessor(protocolProcessor);
		server.start();
		
		IRpcClient client = factory.getSocketClient(hostAddrss, portNumber, protocolProcessor);
		client.addReturnedClass("getDate", DateInfo.class);
		client.addReturnedClass("getSystemInfo", SystemInfo.class);
		client.addReturnedClass("square", null);
		client.addReturnedClass("stringConcat", String.class);

		
		
		System.out.println("Client starts sending request----------------------");
		
		client.start();
		
		String received = (String)client.invoke("getDate");
		System.out.println(received);
		received = (String)client.invoke("square", 1.1);
		System.out.println(received);
		received = (String)client.invoke("getSystemInfo");
		System.out.println(received);
		received = (String)client.invoke("stringConcat", "asdf", "4356");
		System.out.println(received);
		
		// invalid method request from client or to server 
		received = (String)client.invoke("stringConct", "asdf", "1234");
		System.out.println(received);
		
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
