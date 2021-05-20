package project.rpc.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;

import project.rpc.factory.format.IFormatProcessor;
import project.rpc.factory.format.JsonFormatProcessor;
import project.rpc.factory.protocol.AbstractProtocolProcessor;
import project.rpc.factory.protocol.DefaultProtocolProcessor;

/**
 * The implementation of socket client that connects to the socket server for service invocation.   
 *
 */
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
	
	/**
	 * Initialize the socket client with server info. 
	 * @param hostAddress the host address of the socket server (either IP or URL address). 
	 * @param portNumber the port number of the socket server. 
	 */
	public RpcSocketClient(final String hostAddress, final int portNumber) {		
		this.hostAddress = hostAddress;
		this.portNumber = portNumber;
	}
	
	/**
	 * Setup the class type of protocol processor used for encoding/decoding protocolary message. 
	 * @param type the Class type used for creating protocol processor.
	 * @return the instance of this socket client. 
	 */
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
	
	/**
	 * Setup the class type of format processor used for encoding/decoding service request and reply. 
	 * @param type the Class type used for creating format processor.
	 * @return the instance of this socket client. 
	 */
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
	
	/**
	 * Start client socket connection. 
	 */
	@Override
	public void start() {
		try {
			// establish the connection with server host address and port.
			InetAddress ip = InetAddress.getByName(hostAddress);
			socket = new Socket(ip, portNumber);
			// Create a ProtocolProcessor used for communication. 
			try {
				protocolProcessor = protocolProcessorClass
						.getDeclaredConstructor(InputStream.class, OutputStream.class)
						.newInstance(socket.getInputStream(), socket.getOutputStream());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				protocolProcessor = null;
				e.printStackTrace();
				return;
			}
			// Create a FormatProcessor. 
			try {
				formatProcessor = formatProcessorClass.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				formatProcessor = null;
				e.printStackTrace();
				return;
			}
			// Confirming ready.
			protocolProcessor.waitReadyBlocking();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stop client socket connection. 
	 */
	@Override
	public void stop() {
		try {
			if(isConnected()) {
				protocolProcessor.writeExit();
			}
			// closing resources completely
			closeResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close all resources related to the socket client. 
	 * @throws IOException if encountering a problem in closing Socket. 
	 */
	private void closeResource() throws IOException {
		if(protocolProcessor != null) {
			protocolProcessor.close();
		}
		if(socket != null) {
			socket.close();
		}
	}
	
	/**
	 * Check if the connection of socket client has been closed. 
	 * @return true if the connection socket client has been closed; false otherwise. 
	 */
	private boolean isConnected() {
		return (protocolProcessor != null && !protocolProcessor.isclosed()) && 
				(socket != null && !socket.isClosed());
	}
	
	/**
	 * Invoke designated service with required parameter. 
	 * @param service the name of the service. 
	 * @param typeClass the designated returned type. 
	 * @param params the required parameter of the service. 
	 * @return the object returned by the service. 
	 */
	@Override
	public Object invoke(String method, Class<?> typecClass, Object... params) {
		// Check socket connection. 
		if(!isConnected()) {
			System.err.println("Connection is closed!");
			return null;
		}
		
		String message;
		/// Request generating and sending. -----
		// Generate request message. 
		try {
			// Returned type with default constructor. 
			Constructor<?> constructor = typecClass.getDeclaredConstructor();
			message = formatProcessor.encode(method, constructor.newInstance(), params);
		} catch (NoSuchMethodException | SecurityException |InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NullPointerException e) {
			// Returned type without default constructor. 
			message = formatProcessor.encode(method, null, params);
		}
		
		// Encapsulate message according with protocol and send it. 
		protocolProcessor.write(message);
		
		/// Reply fetching and decoding. -----
		// Wait for response. 
		String received = protocolProcessor.readAndDecode();

//		System.out.println("Client receive: " + received);
		
		// Check if received message is exit signal.
		if(protocolProcessor.isExit(received)) {
			System.out.println("Client receive exit message!");
			try {
				closeResource();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		else {
			// Decode "return" section from received message with returned type. 
			return formatProcessor.decodeReturnVal(received, typecClass);
		}
		
	}

}
