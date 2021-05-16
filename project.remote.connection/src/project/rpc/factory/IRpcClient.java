package project.rpc.factory;

public interface IRpcClient {
	public void start();
	public void stop();
	/*
	 * Be able to receive arbitrary length of arguments and  
	 * retrieve arguments and assign to JSON field for request invocation. 
	 */
    public Object invoke(String service, Object... params);
    
    public void addReturnedClass(String service, Class<?> objectClass);
}