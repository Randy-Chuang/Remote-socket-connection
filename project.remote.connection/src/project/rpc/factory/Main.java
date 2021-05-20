package project.rpc.factory;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;
import project.remote.server.service.SystemService;

public class Main {
	public static void main(String[] args) {
		String hostAddrss = "127.0.0.1";
		int portNumber = 5056;

		/// server code
		SystemService systemService = new SystemService();
		RpcFactory factory = RpcFactory.getInstance();
//		IRpcServer server = factory.getJsonSocketServer(portNumber);
		IRpcServer server = factory.getXmlSocketServer(portNumber);
		server.addRequestHandler("getDate", null, (ctx) -> {
			ctx.returnVal = systemService.getDate();
		});
		server.addRequestHandler("getSystemInfo", null, (ctx) -> {
			ctx.returnVal = systemService.getSystemInfo();
		});
		server.<Double>addRequestHandler("square", Double.class, (ctx) -> {
			ctx.returnVal = systemService.square(ctx.param);
		});
		server.start();

		/// client code
//		IRpcClient client = factory.getJsonSocketClient(hostAddrss, portNumber);
		IRpcClient client = factory.getXmlSocketClient(hostAddrss, portNumber);

		System.out.println("Client starts sending request----------------------");

		client.start();
		// invoke() would return the class object associated with the service
		DateInfo dateInfo = (DateInfo)client.invoke("getDate", DateInfo.class);
		if(dateInfo != null) {
			dateInfo.printContent();
		}
		
		Double square = (Double)client.invoke("square", Double.class, 1.5);
		if(square != null) {
			System.out.println("--Square--\n" + square);
		}
		
		SystemInfo systemInfo = (SystemInfo)client.invoke("getSystemInfo", SystemInfo.class);
		if(systemInfo != null) {
			systemInfo.printContent();
		}
		
		client.stop();

		System.out.println("Client closes socket connection ----------------------");

		/// server code: stop server
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		server.stop();
	}
	
}
