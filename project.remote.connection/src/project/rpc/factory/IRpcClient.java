package project.rpc.factory;

/**
 * The interface to interact with a client from a user perspective. 
 */
public interface IRpcClient {
	/**
	 * Start client connection. 
	 */
	public void start();
	/**
	 * Stop client connection. 
	 */
	public void stop();
	/**
	 * Invoke designated service with required parameter. 
	 * @param service the name of the service. 
	 * @param typeClass the designated returned type. 
	 * @param params the required parameter of the service. 
	 * @return the object returned by the service. 
	 */
    public Object invoke(String service, Class<?> typeClass, Object... params);
}