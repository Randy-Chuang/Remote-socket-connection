package project.rpc.factory.format;

/**
 * The interface used for encoding / decoding message according to a specific format. 
 *
 */
public interface IFormatProcessor {
	/**
	 * Decode the service (method) name from formatted message.
	 * @param message the formatted message. 
	 * @return service (method) name.
	 */
	public String getMethod(final String message);
	/**
	 * Encode the given info into a formatted message. 
	 * @param method the service (method) name field in formatted message.
	 * @param returnVal the object for return field in formatted message. 
	 * @param param the object for parameter field in formatted message.  
	 * @return the encoded formatted message.
	 */
	public String encode(String method, Object returnVal, Object... param);
	/**
	 * Decode parameter from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing parameter. 
	 * @return the parameter object.
	 */
	public Object decodeParam(final String message, Class<?> typeClass);
	/**
	 * Decode return value from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing return value. 
	 * @return the returned object. 
	 */
	public Object decodeReturnVal(final String message, Class<?> typeClass);
	/**
	 * Convert the formatted message into a prettier formatted string for printing. 
	 * @param message the formatted message. 
	 * @return a prettier formatted string. 
	 */
	public String prettyOutput(final String message);
}
