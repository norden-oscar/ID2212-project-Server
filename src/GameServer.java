import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class GameServer implements Runnable {

	private Socket clientSocket1, clientSocket2;
	private PrintWriter out1,out2;
	private BufferedReader in1,in2;
	protected State state;
	private ServerSocket serverSocket;
	private int portNumber=0;
	private Lobby lobby;

	public GameServer(Lobby lobby) {
		state = State.EMTPY;
		Random rand = new Random();
		portNumber = rand.nextInt(1000) + 3000;
		this.lobby = lobby;
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void run() {	
		searchingForPlayers();
		initializeGame();
		playingGame();

	}
	
	public int getPortNumber(){
		return portNumber;
	}

	 private void playingGame() {
		// TODO Själva spel logiken och main spel loopen

	}

	private void searchingForPlayers() {
		boolean keepSearching = true;
		// TODO Väntar på 2 spelare,
		while(keepSearching){
			if(!(clientSocket1.isBound())&&!(clientSocket2.isBound())){
				try {
					clientSocket1 = serverSocket.accept();
					in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
					out1 = new PrintWriter(clientSocket1.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				state = State.IDLE;
			}
			else if(clientSocket1.isBound() && !(clientSocket2.isBound())){
				try {
					clientSocket2 = serverSocket.accept();
					in2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
					out2 = new PrintWriter(clientSocket2.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				keepSearching = false;
				state = State.FULL;
			}
		}
		
	}

	private void initializeGame() {
		// TODO skapar serversocketen och datatstrukturer som behövs under
		// spelets gång
	}
}
