package project.remote.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import project.remote.server.service.SystemService;
import project.remote.server.service.ISystemService.DateInfo;

public class ClientHandlerRunnable implements Runnable {
	private final Socket s;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	
	private final SystemService serverService;

	// Constructor
	public ClientHandlerRunnable(Socket s) throws IOException {
		this.s = s;
		// obtaining input and out streams
		this.dis = new DataInputStream(s.getInputStream());
		this.dos = new DataOutputStream(s.getOutputStream());
		
		this.serverService = new SystemService();
	}

	@Override
	public void run() {
		String received;
		String toreturn;
		
		while (true) {
			try {

				// Ask user what he wants
				dos.writeUTF("What do you want?[Date | Time]..\n" + "Type Exit to terminate connection.");

				// receive the answer from client
				received = dis.readUTF();

				if (received.equals("Exit")) {
					System.out.println("Client " + this.s + " sends exit...");
					System.out.println("Closing this connection.");
					this.s.close();
					System.out.println("Connection closed");
					break;
				}

				// write on output stream based on the
				// answer from the client
				switch (received) {

				case "Date":
					DateInfo dateInfo = serverService.getDate();
					dos.writeUTF(dateInfo.year);
					break;

				case "Time":
//					toreturn = serverService.getTime();
//					dos.writeUTF(toreturn);
					break;

				default:
					dos.writeUTF("Invalid input");
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			// closing resources
			this.dis.close();
			this.dos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}