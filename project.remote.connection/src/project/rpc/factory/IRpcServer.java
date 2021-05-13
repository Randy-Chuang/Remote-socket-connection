package project.rpc.factory;

public interface IRpcServer {
    public void start();  // start server
    public void stop();  // stop server
    /*
     * Handler should assign returnVal if needed.
     */
    public void addRequestHandler(String name, Invocable r); 
    public void addProtocolProcessor(AbstractProtocolProcessor processor);
    
}