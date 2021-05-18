package project.rpc.factory.format;

public interface IFormatProcessor {

	public String getMethod(final String message);
	
	public Object toObjectFormat(final String message);
	
	public String encode(final Object refRequest, final String methodName, Object returnVal, Object... param);
	
	public String encode(final String refRequest, final String methodName, Object returnVal, Object... param);
	
	public Object decodeParam(final String message, Class<?> typeClass);
	
	public Object decodeReturnVal(final String message, Class<?> typeClass);
	
	public String prettyOutput(final String message);
}
