package project.rpc.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

public class RpcSocketClient implements IRpcClient {
	private final String hostAddress;
	private final int portNumber;
	private Socket socket;
	// ProtocolProcessor
	private AbstractProtocolProcessor protocolProcessor = null;
	private Class<? extends AbstractProtocolProcessor> protocolProcessorClass = DefaultProtocolProcessor.class;
	// FormatProcessor 
	private IFormatProcessor formatProcessor = null;
	private Class<? extends IFormatProcessor> formatProcessorClass = JsonFormatProcessor.class;
	// Returned type of different services
	private final Map<String, Class<?>> returnedClassMap = new TreeMap<String, Class<?>>();
	
	public RpcSocketClient(final String hostAddress, final int portNumber) {		
		this.hostAddress = hostAddress;
		this.portNumber = portNumber;
	}
	
	public RpcSocketClient setProtocolProcessor(Class<? extends AbstractProtocolProcessor> type) {
		if(type.getCanonicalName().equals(AbstractProtocolProcessor.class.getCanonicalName())) {
			System.err.println(AbstractProtocolProcessor.class.getCanonicalName() + ", the base abstract class contains "
					+ "several abstract methods which are not compatible in practice of protocol processing and control.");
			System.err.println(protocolProcessorClass.getCanonicalName() + ", the default protocol process would be adopted instead.");
		}
		else {
			protocolProcessorClass = type;
		}
		return this;
	}
	
	public RpcSocketClient setFormatProcessor(Class<? extends IFormatProcessor> type) {
		if(type.getCanonicalName().equals(IFormatProcessor.class.getCanonicalName())) {
			System.err.println(IFormatProcessor.class.getCanonicalName() + ", the base interface contains "
					+ "several unimplemented methods which are not compatible in practice of format processing.");
			System.err.println(formatProcessorClass.getCanonicalName() + ", the default format process would be adopted instead.");
		}
		else {
			formatProcessorClass = type;
		}
		return this;
	}
	
	
	@Override
	public void start() {
		try {
			// establish the connection with server IP address and port.
			InetAddress ip = InetAddress.getByName(hostAddress);
			socket = new Socket(ip, portNumber);

			try {
				protocolProcessor = protocolProcessorClass.getDeclaredConstructor(InputStream.class, OutputStream.class).newInstance(socket.getInputStream(), socket.getOutputStream());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				protocolProcessor = null;
				e.printStackTrace();
				return;
			}
			
			try {
				formatProcessor = formatProcessorClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				formatProcessor = null;
				e.printStackTrace();
				return;
			}
			
			// Confirming ready.
			protocolProcessor.waitOkBlocking();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object invoke(String method, Object... params) {
		if(protocolProcessor.isclosed()) {
			System.err.println("Connection is closed");
			return null;
		}
		
		String tosend, message;
		try {
			Constructor<?> constructor = returnedClassMap.get(method).getDeclaredConstructor();
			message = formatProcessor.encode(null, method, constructor.newInstance(), params);
		} catch (NoSuchMethodException | SecurityException |InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NullPointerException e) {
			message = formatProcessor.encode(null, method, null, params);
		}		
		tosend = protocolProcessor.encode(message);

		// Send request
		protocolProcessor.write(tosend);
		
		String received = protocolProcessor.readResponseBlocking();
		
		System.out.println("Client received:----\n" + formatProcessor.prettyOutput(received));
		
		if(protocolProcessor.isExit(received)) {
			try {
				closeResource();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return received;
		}
		else {
			return formatProcessor.decodeReturnVal(received, returnedClassMap.get(method));
		}
		
	}

	@Override
	public void stop() {
		try {
			if(!isConnectionClosed()) {
				protocolProcessor.writeExit();
			}
			// closing resources completely
			closeResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeResource() throws IOException {
		protocolProcessor.close();
		protocolProcessor = null;
		formatProcessor = null;
		if(socket != null) {
			socket.close();
			socket = null;
		}
	}
	
	private boolean isConnectionClosed() {
		return protocolProcessor.isclosed() && socket == null;
	}

	/*
	 * Used for decoding returned object and generating request message. 
	 */
	@Override
	public void addReturnedClass(String name, Class<?> objectClass) {
		// Check for duplication of Class type
		if (returnedClassMap.containsKey(name)) {
			System.err.println("Trying to override the existing Class type: " + name);
			return;
		}
		
		try {
			if(objectClass != null) {
				objectClass.getDeclaredConstructor();
			}
		} catch (Exception e) {
			System.err.println(objectClass.getCanonicalName() + " doesn't have a default constructor. "
					+ "Therefore, the \"return\" section in request message wouldn't be anything but a null keyword.");
		}
		
		// Add Class type
		returnedClassMap.put(name, objectClass);
	}

}
