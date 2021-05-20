package project.rpc.factory;

/**
 * This is a functional interface and can therefore be used as the assignment target 
 * for a lambda expression or method reference.
 * 
 * @param <T> the generic type parameter that addresses the type of data members in an instance of <b>InvocationContext</b>.
 * @see project.rpc.factory.InvocationContext
 */
public interface Invocable <T> {    
	/**
	 * Invoking this function represents invoking a registered service.
	 * <p>
	 * The related info shall be retrieved from or stored into <b>InvocationContext</b>.
	 * @param requestContext the required info that are related to invoke this method (registered service). 
	 */
    public void invoke(InvocationContext<T> requestContext);
}
