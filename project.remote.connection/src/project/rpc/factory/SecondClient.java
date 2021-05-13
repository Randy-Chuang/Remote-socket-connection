package project.rpc.factory;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;

public class SecondClient {
	
	

	public static void main(String[] args) {
		
		
		
		String hostAddrss = "127.0.0.1";
		int portNumber = 5056;
		
//		JsonRpcSocketClient clienta = new JsonRpcSocketClient(hostAddrss, portNumber, DefaultProtocolProcessor.class);
		
		DefaultProtocolProcessor protocolProcessor = new DefaultProtocolProcessor();
		RpcFactory factory = RpcFactory.getSocketInstance();
		
		
		IRpcClient client = factory.getSocketClient(hostAddrss, portNumber, protocolProcessor);
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
		client.start();
		
		String received = (String)client.invoke("square", 1.1);
		System.out.println(received);
		received = (String)client.invoke("getDate");
		System.out.println(received);
		received = (String)client.invoke("getSystemInfo");
		System.out.println(received);
		
		client.stop();
	}
}
