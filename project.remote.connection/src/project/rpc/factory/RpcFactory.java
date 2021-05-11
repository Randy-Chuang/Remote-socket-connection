package project.rpc.factory;

import java.io.IOException;
import java.net.Socket;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageDecode;
import project.remote.server.service.ServerService;


/*
 * TODO: 
 * Time out mechanism. 
 */
public class RpcFactory {
	
	
	
	private class JsonRpcSocketClient implements IJsonRpcClient{
		private Socket clientSocket;
		@Override
		public Object invoke(String method, Object... params) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	
	
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
	
	public IJsonRpcClient getSocketClient() {
		return null;
	}
	
	public static void main(String[] args) {
		/// server code
		ServerService serverService = new ServerService();
		RpcFactory factory = RpcFactory.getSocketInstance();
		IJsonRpcServer server = factory.getSocketServer(5056);
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
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		server.stop();
	}
}
