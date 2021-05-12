package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import project.remote.common.service.IOUtility;
import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageField;
import project.remote.common.service.NetMessage;

public class JsonRpcSocketClient implements IJsonRpcClient{
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final Socket socket;
	private AbstractProtocolProcessor processor;
	private final Map<String, Function<Object[], JsonObject>> requestGeneratorMap = new TreeMap<String, Function<Object[], JsonObject>>();
	
	public JsonRpcSocketClient(final String hostAddress, final int portNumber, final AbstractProtocolProcessor processor) throws IOException {		
		InetAddress ip = InetAddress.getByName(hostAddress);
		
		// establish the connection with server ip address and port
		this.socket = new Socket(ip, portNumber);
		
		// Encapsulate StdIn / StdOut for Process. 
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		// used for protocol processing
		this.processor = processor;
		
		// Confirming ready.
		processor.waitOkBlocking(reader);
	}
	
	@Override
	public Object invoke(String method, Object... params) {
		
		Function<Object[], JsonObject> designatedgenerator = requestGeneratorMap.get(method);
		if(designatedgenerator == null) {
			System.err.println("Request generator not found: " + method);
			return null;
		}
		JsonObject jsonRequest = designatedgenerator.apply(params);
		if(jsonRequest == null) {
			System.err.println("Null pointer for JsonObject.");
			return null;
		}
		
		String tosend;
		try {
			tosend = NetMessage.netMessageEncode(jsonRequest);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		
		// Send request
		processor.write(writer, tosend);

		return processor.readResponseBlocking(reader);
	}

	@Override
	public void stop() {
		try {
			processor.writeExit(writer);
			// closing resources
			reader.close();
			writer.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void addRequestGenerator(String name, Function<Object[], JsonObject> mapper) {
		// Check for duplication of services
		if (requestGeneratorMap.containsKey(name)) {
			System.err.println("Trying to override the existing request generator: " + name);
			return;
		}
		// Add service
		requestGeneratorMap.put(name, mapper);
	}

	
}
