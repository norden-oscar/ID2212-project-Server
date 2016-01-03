import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lobby {
	public static void main(String[] args) {
		int poolSize = 3;
		int portNumber = 1337;
		ArrayList<Gameserver> gamelist = new ArrayList<GameServer>();
		
		try { 							// hantera första inparametern
			poolSize = Integer.parseInt(args[0]);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("using standard value for poolsize :" + poolSize);
		} catch (java.lang.NumberFormatException nfe) {
			System.err.println("poolsize parameter must be a number");
			System.exit(1);
		}
		
		try { 								// hantera den andra inparametern
			portNumber = Integer.parseInt(args[1]);
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			System.out.println("using standard value for port number :" + portNumber);
		} catch (java.lang.NumberFormatException nfe) {
			System.err.println("Port number parameter must be a number");
			System.exit(1);
		}
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(poolSize);
		ServerSocket listen;
		try {
			System.out.println("starting server with maximum " + poolSize
					+ " threads at a time that is accepting requests on port: " + portNumber);
			listen = new ServerSocket(portNumber);
			while (true) {
				Socket clientSocket = listen.accept();
				System.out.println("client connected");
				executor.execute(new GameServer(clientSocket));
			}
		} catch (IOException ex) {
			System.out.println("cannot listen to port :" + portNumber);
			System.exit(1);
		}
	}
}
