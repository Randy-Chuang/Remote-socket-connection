package project.rpc.factory;


public interface IJsonRpcClient {
	/*
	 * Be able to receive arbitrary length of arguments and  
	 * retrieve arguments and assign to JSON field for request invocation. 
	 */
    public Object invoke(String method, Object... params);
}