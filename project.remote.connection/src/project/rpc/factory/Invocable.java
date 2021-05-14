package project.rpc.factory;

public interface Invocable <T> {    
    public void invoke(InvocationContext<T> requestContext);
}
