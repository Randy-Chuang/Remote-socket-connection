package project.rpc.factory;

public interface IFormatProcessor {

	public String getMethod(final String message);
	
	public Object toObjectFormat(final String message);
	
	public String encode(final Object refRequest, final String methodName, Object returnVal, Object... param);
	
	public String encode(final String refRequest, final String methodName, Object returnVal, Object... param);
	
//	public String encodeReply(final Object refRequestObject, Object returnVal);
//	
//	public String encodeReply(final String refRequestMessage, Object returnVal);
	
	public Object decodeParam(final String message);
	
	public Object decodeReturnVal(final String message);
	
	public String prettyOutput(final String message);
	
	public void addMethodReturnType(final String method, Class<?> type);
	
	public Class<?> getMethodReturnType(final String method);
	
	public void addMethodParamType(final String method, Class<?> type);
	
	public Class<?> getMethodParamType(final String method);
}
