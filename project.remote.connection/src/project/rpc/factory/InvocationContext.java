package project.rpc.factory;

/**
 * This Class contains the required informations to invoke a registered server service. 
 * 
 * @param <T> the type of parameter to invoke a registered service.
 * @see project.rpc.factory.Invocable
 */
public class InvocationContext <T> {
	/**
	 * The parameter to invoke a registered service. 
	 */
	public T param;
	/**
	 * The returned object left by invoking a registered service. 
	 */
    public Object returnVal;
}
