package project.rpc.factory.format;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A format processor to encode / decode message to / from XML format. 
 * 
 */
public class XmlFormatProcessor implements IFormatProcessor {
	public static class XmlMessage{
		public String method;
		public Object paramObject, retObject;
	}
	
	private static String serializeToString(XmlMessage xmlMessage) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    XMLEncoder encoder = new XMLEncoder(baos);
	    encoder.writeObject(xmlMessage);
	    encoder.close();
	    String string = new String(baos.toByteArray());
	    try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return string;
	}

	private static XmlMessage deserializeFromString(String string) {
		ByteArrayInputStream bais = new ByteArrayInputStream(string.getBytes());
	    XMLDecoder decoder = new XMLDecoder(bais);
	    XmlMessage xmlMessage = (XmlMessage) decoder.readObject();
	    decoder.close();
	    try {
			bais.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return xmlMessage;
	}
	
	/**
	 * Decode the service (method) name from formatted message.
	 * @param message the formatted message. 
	 * @return service (method) name.
	 */
	@Override
	public String getMethod(final String message) {	
		XmlMessage xmlMessage = deserializeFromString(message);
		return xmlMessage.method;
	}
	
	/**
	 * Encode the given info into a formatted message. 
	 * @param method
	 * @param returnVal
	 * @param param
	 * @return
	 */
	@Override
	public String encode(String method, Object returnVal, Object... param) {
		XmlMessage xmlMessage = new XmlMessage();
		xmlMessage.method = method;
		xmlMessage.retObject = returnVal;
		if(param != null && param.length == 1) {
			xmlMessage.paramObject = param[0];
		}
		else {
			xmlMessage.paramObject = null;
		}
		
		return serializeToString(xmlMessage);
	}

	/**
	 * Decode parameter from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing parameter. 
	 * @return the parameter object.
	 */
	@Override
	public Object decodeParam(final String message, Class<?> typeClass) {	
		XmlMessage xmlMessage = deserializeFromString(message);
		return xmlMessage.paramObject;
	}

	/**
	 * Decode return value from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing return value. 
	 * @return the returned object. 
	 */
	@Override
	public Object decodeReturnVal(String message, Class<?> typeClass) {
		XmlMessage xmlMessage = deserializeFromString(message);
		return xmlMessage.retObject;
	}

	/**
	 * Convert the formatted message into a prettier formatted string for printing. 
	 * @param message the formatted message. 
	 * @return a prettier formatted string. 
	 */
	@Override
	public String prettyOutput(String message) {	
		return message;
	}
	
}