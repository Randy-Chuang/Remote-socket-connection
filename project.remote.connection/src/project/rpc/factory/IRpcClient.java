package project.rpc.factory;

import java.util.function.Function;

import com.google.gson.JsonObject;

public interface IRpcClient {
	public void start();
	public void stop();
	/*
	 * Be able to receive arbitrary length of arguments and  
	 * retrieve arguments and assign to JSON field for request invocation. 
	 */
    public Object invoke(String method, Object... params);
    
    public void addRequestGenerator(String name, Function<Object[], JsonObject> mapper);
}