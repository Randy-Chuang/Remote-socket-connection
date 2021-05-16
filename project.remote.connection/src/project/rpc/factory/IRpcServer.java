package project.rpc.factory;

public interface IRpcServer {
    public void start();  // start server
    public void stop();  // stop server
    /*
     * Handler should assign returnVal if needed.
     */
    public <T> void addRequestHandler(String service, Invocable <T> r);
    
    public void addParameterClass(String service, Class<?> objectClass);
}