package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import com.google.gson.JsonObject;

import project.remote.common.service.NetMessage;

public class RpcSocketClient implements IRpcClient{
	private final String hostAddress;
	private final int portNumber;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Socket socket;
	private AbstractProtocolProcessor processor;
	private Class<? extends AbstractProtocolProcessor> processorDefaultType = DefaultProtocolProcessor.class;
	private final Map<String, Function<Object[], JsonObject>> requestGeneratorMap = new TreeMap<String, Function<Object[], JsonObject>>();
	
	public RpcSocketClient(final String hostAddress, final int portNumber,  Class<? extends AbstractProtocolProcessor> type) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {		
		this(hostAddress, portNumber);

		if(type.getCanonicalName().equals(AbstractProtocolProcessor.class.getCanonicalName())) {
			System.err.println(AbstractProtocolProcessor.class.getCanonicalName() + ", the base abstract class contains "
					+ "several abstract methods which are not compatible in practice of protocol processing and control.");
			System.err.println(processorDefaultType.getCanonicalName() + ", the default protocol process would be adopted instead.");
		}
		else {
			processorDefaultType = type;
		}
		
		// Caution: what if there are multiple constructor???? even with different arguments.
		this.processor = type.getDeclaredConstructor().newInstance();
	}
	
	public RpcSocketClient(final String hostAddress, final int portNumber, final AbstractProtocolProcessor processor) {		
		this(hostAddress, portNumber);
		// used for protocol processing
		this.processor = processor;
	}
	
	public RpcSocketClient(final String hostAddress, final int portNumber) {		
		this.hostAddress = hostAddress;
		this.portNumber = portNumber;
	}
	
	@Override
	public void start() {
		try {
			InetAddress ip = InetAddress.getByName(hostAddress);
			
			// establish the connection with server ip address and port
			socket = new Socket(ip, portNumber);

			// Encapsulate StdIn / StdOut for Process.
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			// Confirming ready.
			processor.waitOkBlocking(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object invoke(String method, Object... params) {
		if(writer == null || reader == null) {
			System.err.println("Connection is closed");
			return null;
		}

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
		
		String received = processor.readResponseBlocking(reader);
		
		if(processor.isExit(received)) {
			try {
				closeResource();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return received;
	}

	@Override
	public void stop() {
		try {
			if(!isConnectionClosed()) {
				processor.writeExit(writer);
			}
			// closing resources completely
			closeResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeResource() throws IOException {
		if(reader != null) {
			reader.close();
			reader = null;
		}
		if(writer != null) {
			writer.close();
			writer = null;
		}
		if(socket != null) {
			socket.close();
			socket = null;
		}
	}
	
	private boolean isConnectionClosed() {
		return reader == null && writer == null && socket == null;
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
