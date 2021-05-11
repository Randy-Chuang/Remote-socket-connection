Remote Program Implementation
====

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