package project.rpc.factory;

/**
 * The interface to interact with a server from a user perspective. 
 */
public interface IRpcServer {
	/**
	 * Start server.
	 */
    public void start(); 
    /**
     * Stop server. 
     */
    public void stop();
    /**
     * Add a service to server. 
     * @param <T> the parameter type for this service. 
     * @param service the name of service. 
     * @param paramClass the class of parameter type for this service. 
     * @param r the actual instance that are going to handle this service. 
     */
    public <T> void addRequestHandler(String service, Class<T> paramClass, Invocable <T> r);
}