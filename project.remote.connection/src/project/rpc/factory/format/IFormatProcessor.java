package project.rpc.factory.format;

/**
 * The interface to encode / decode message according to a specific format. 
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
	 * Convert the formatted message into object associated with the adopted parser. 
	 * @param message the formatted message. 
	 * @return object associated with the adopted parser.
	 */
	public Object toObjectFormat(final String message);
	/**
	 * Encode the given info of request / reply into a formatted message. 
	 * <p> 
	 * This is a generalized method to encode for request or encode reply with reference of request. 
	 * @param refRequest  
	 * @param methodName 
	 * @param returnVal
	 * @param param
	 * @return
	 */
	public String encode(final Object refRequest, final String methodName, Object returnVal, Object... param);
	/**
	 * Encode the given info of request / reply into a formatted message. 
	 * <p> 
	 * This is a generalized method to encode for request or encode reply with reference of request. 
	 * @param refRequest
	 * @param methodName
	 * @param returnVal
	 * @param param
	 * @return
	 */
	public String encode(final String refRequest, final String methodName, Object returnVal, Object... param);
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
