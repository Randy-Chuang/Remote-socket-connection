package project.rpc.factory;

import java.io.IOException;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageDecode;
import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.server.service.ServerService;


/*
 * TODO: 
 * 1. Closing server and closing all the services (closing all child threads with thread pool??) 
 * 1.5. The thread of client handler may be blocked with specific methods. e.g. a blocking reading method
 * 2. Manage your design of factory pattern with a clearer structure, current class hierarchy is showing below:
 * 		Factory -> JsonRpcServer/Client -> ProtocolProcessor (read, write, encode/decode NetMessage)
 * 										-> (Client) RequestGenerator (generate JsonRequest with given arguments of objects)
 * 										-> (Server) RequestHandler (Invoke corresponding service and output a replied JsonObject)
 * 5. Restructure
 * 80. Confirming that accessing same object with multiple threads won't produce any problem.
 * 90. Time out mechanism. 
 */
public class RpcFactory {
	
	public static RpcFactory getSocketInstance() {
		return new RpcFactory();
	}
	
	public IJsonRpcServer getSocketServer(int portNumber) {
		try {
			return new JsonRpcSocketServer(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IJsonRpcClient getSocketClient(String hostAddr , int portNumber, AbstractProtocolProcessor processor) {
		try {
			return new JsonRpcSocketClient(hostAddr, portNumber, processor);
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String[] args) {
		String hostAddrss = "127.0.0.1";
		int portNumber = 5056;
		
		/// server code
		ServerService serverService = new ServerService();
		RpcFactory factory = RpcFactory.getSocketInstance();
		IJsonRpcServer server = factory.getSocketServer(portNumber);
		server.addRequestHandler("getDate", (ctx) -> {
			JsonObject jsonRequest = MessageDecode.getJsonObject(ctx.rawRequest);
			ctx.returnVal = serverService.getServerDate(jsonRequest);
		});
		
		server.addRequestHandler("getSystemInfo", (ctx) -> {
			JsonObject jsonRequest = MessageDecode.getJsonObject(ctx.rawRequest);
			ctx.returnVal = serverService.getServerSystemInfo(jsonRequest);
		});
		server.addRequestHandler("square", (ctx) -> {
			JsonObject jsonRequest = MessageDecode.getJsonObject(ctx.rawRequest);
			ctx.returnVal = serverService.getServerSquare(jsonRequest);
		});
		
		MyProtocolProcessor protocolProcessor = new MyProtocolProcessor();
		server.addProtocolProcessor(protocolProcessor);
		server.start();
		
		
		IJsonRpcClient client = factory.getSocketClient(hostAddrss, portNumber, protocolProcessor);
		client.addRequestGenerator("getDate", (params) -> {
			if(params.length == 0) {
				return MessageEncode.encodeDateInfo(null, null);
			}
			else {
				System.err.println("Invalid length of input parameters."); 
				return null;
			}
		});
		client.addRequestGenerator("getSystemInfo", (params) -> {
			if(params.length == 0) {
				return MessageEncode.encodeSystemInfo(null, null);
			}
			else {
				System.err.println("Invalid length of input parameters."); 
				return null;
			}
		});
		client.addRequestGenerator("square", (params) -> {
			// input may be an non-double number
			System.out.println("Square req parameter is instanceof Double: " + (params[0] instanceof Double));
			if(params.length == 1 && params[0] instanceof Double) {
				JsonObject jsonRequest = MessageEncode.encodeSquare(null, null);
				jsonRequest.addProperty(MessageField.PARAMETERS_OBJ_STRING, (Double)params[0]);
				return jsonRequest;
			}
			else {
				System.err.println("Invalid length of input parameters."); 
				return null;
			}
		});
		
		
		System.out.println("Client starts sending request----------------------");
		
		client.invoke("square", 1.123);
		client.invoke("getDate");
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
