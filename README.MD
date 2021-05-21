Remote Program Implementation
====

## User Guide for using Factory mode

### Project structure 

#### File hierarchy

```
project
    ├── remote
    │   ├── common
    │   │   └── service
    │   │       ├── LspBaseProtocol.java
    │   │       └── ServiceClass.java
    │   └── server
    │       ├── FactoryDemo.java
    │       └── service
    │           ├── ISystemService.java
    │           └── SystemService.java
    └── rpc
        └── factory
            ├── format
            │   ├── IFormatProcessor.java
            │   ├── JsonFormatProcessor.java
            │   └── XmlFormatProcessor.java
            ├── Invocable.java
            ├── InvocationContext.java
            ├── IRpcClient.java
            ├── IRpcServer.java
            ├── protocol
            │   ├── AbstractProtocolProcessor.java
            │   └── DefaultProtocolProcessor.java
            ├── RpcFactory.java
            ├── RpcSocketClient.java
            └── RpcSocketServer.java
```

#### Package hierarchy of Factory Pattern

- `project.rpc.factory`: 
   This package contains implementation of factory pattern. On top level, it provides the APIs to help user obtain the instance of server or client. 
    - `project.rpc.factory.format`
        This package contains the implementation of **Format Processor**, which encodes and decodes the formatted message. Currently supported format:
        - Json format (default)
        - Xml format 
        
        User could specify the format being used in the top level APIs provided by class **RpcFactory**.
    - `project.rpc.factory.protocol`
        This package contains the implementation of **Protocol Processor**, which encodes / decodes the message transmitted through the network and provides the interfaces to send / fetch message actually. Currently supported format:
        - Microsoft LSP base protocol (default)
        
        User could specify the protocol being used in the top level APIs provided by class **RpcFactory**.

### Factory mode in use

- Obtain instance of `RpcFactory`:
    ```java
    RpcFactory factory = RpcFactory.getInstance();
    ```
- Socket server setup (specifying **Json** format): 
    ```java
    IRpcServer server;
	try {
        // Obtain server instance
	    server = factory.getJsonSocketServer(5056);
	} catch (IOException e) {
	    e.printStackTrace();
		return;
	}
    // instance that provides the methods of service. <-- **Caution**
    SystemService systemService = new SystemService();
    // Register server services. 
    server.addRequestHandler("getDate", null, (ctx) -> {
	    ctx.returnVal = systemService.getDate();
	});
	server.addRequestHandler("square", Double.class, (ctx) -> {
        // java performs generic inference to determine the type of param from class type argument 
	    ctx.returnVal = systemService.square(ctx.param);
	});
    
	server.start();
    // Wait for a period of time to provide services. 
    server.stop();
    ```
    - Caution: noted that, if a **RpcServer** handle client requests in multithreading, the registered service should also be **thread-safe**. For instance: 
       ```java 
        server.addRequestHandler("getDate", null, (ctx) -> {
            SystemService systemService = new SystemService();
            ctx.returnVal = systemService.getDate();
        });
       ```
- Socket client setup (specifying **Json** format): 
    ```java
    IRpcClient client = factory.getJsonSocketClient("127.0.0.1", 5056);

	client.start();
	// invoke() would return the class object designated by the class type argument
	DateInfo dateInfo = (DateInfo)client.invoke("getDate", DateInfo.class);
	// do something with retured object. 
	Double square = (Double)client.invoke("square", Double.class, 1.5);
    
    client.stop();
    ```
## Socket connection 

- Server side: specifying port to listening (default to 5056).
    ```sh
    java -jar Server.jar [port number]
    ```
- Client side: specifying port to connect on localhost (default to 5056).
    ```sh
    java -jar Client.jar [port number]
    ```

Quick test: 
```sh
java -jar Server.jar
java -jar Client.jar
```
## Process communication via StdIn / StdOut

Specifying the executable (jar) file of server to run. 
```sh
java -jar ClientLocal.jar [path to executable]
```
Quick test: 
```sh
java -jar ClientLocal.jar ServerLocal.jar
```
## TODOs

- [ ] Supporting multiple input parameters. 
- [ ] Complete mechanism to handle / resolve any possible scenario occurred in practice. 
    Mechanism: Time out, etc. 
    Scenario: unexpected disconnection (without receiving exit message), wrong format (currently, throw exception and exit)
- [ ] Improve factory pattern for ease of use or extending for developer. 
- [ ] Is is good to place javadoc along with your project? (most of the files are html)
## References 

- [Microsoft LSP](https://microsoft.github.io/language-server-protocol/specifications/specification-current/)
- [GSON](https://github.com/google/gson)
    - [GSON 2.8.2 doc](https://javadoc.io/doc/com.google.code.gson/gson/2.8.2/index.html)
