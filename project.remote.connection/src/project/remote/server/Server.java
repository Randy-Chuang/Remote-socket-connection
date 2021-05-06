package project.remote.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * TODO: 
 * 1. Sending length for socket programming
 * 2. Message protocol header you defined. 
 * 90. Mechanism: Unexpected disconnection from Client (time out and ??)
 * 91. Mechanism: to close all the connections and exit.
 * 		(Server is always waiting for further connection ServerSocket.accept() method)
 */

public class Server {
	
	public static void main(String[] args) throws IOException {
		int port = 5056;
		
		// server is listening on specific port number. 
		ServerSocket ss = new ServerSocket(port);

		// infinite loop for getting client request
		while (true) {
			Socket s = null;
			try {
				// socket object to receive incoming client requests
				s = ss.accept();

				System.out.println("A new client is connected : " + s);
				System.out.println("Assigning new thread for this client");

				// create a new Runnable, Thread object and start the thread. 
				Runnable runnable = new ClientHandlerRunnable(s);
				Thread t = new Thread(runnable);
				t.start();

			} catch (Exception e) {
				s.close();
				e.printStackTrace();
			}
		}
	}
}

