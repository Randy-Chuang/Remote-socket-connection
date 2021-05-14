package project.rpc.factory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

import project.remote.common.service.NetMessage;

public class RpcSocketClient implements IRpcClient{
	private final String hostAddress;
	private final int portNumber;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Socket socket;
	private AbstractProtocolProcessor processor;
	private Class<? extends AbstractProtocolProcessor> processorDefaultType = DefaultProtocolProcessor.class;
	private final IFormatProcessor formatProcessor = new JsonFormatProcessor();

	private final Map<String, Class<?>> returnedClassMap = new TreeMap<String, Class<?>>();
	
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
		
		System.out.println("params ? null "+params == null);

		Class<?> objectClass = returnedClassMap.get(method);
		String tosend, message;
		if(objectClass != null) {
			try {
				Constructor<Object> constructor = (Constructor<Object>) returnedClassMap.get(method).getDeclaredConstructor();
				
				message = formatProcessor.encode(null, method, constructor.newInstance(), params);
			} catch (NoSuchMethodException | SecurityException |InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			message = formatProcessor.encode(null, method, null, params);
		}
		
		tosend = NetMessage.netMessageEncode(message);
		
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
	public void addReturnedClass(String name, Class<?> objectClass) {
		// Check for duplication of Class type
		if (returnedClassMap.containsKey(name)) {
			System.err.println("Trying to override the existing Class type: " + name);
			return;
		}
		// Some request doesn't need return object, such as command to server!?
		if(objectClass != null) {
			try {
				Constructor<Object> constructor = (Constructor<Object>)objectClass.getDeclaredConstructor();
			} catch (Exception e) {
				System.err.println("No default constructor for Class: " + objectClass.getCanonicalName());
				e.printStackTrace();
				return;
			}
		}
		// Add Class type
		returnedClassMap.put(name, objectClass);
	}
}
