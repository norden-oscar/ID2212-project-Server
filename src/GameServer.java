import java.net.Socket;

public class GameServer implements Runnable {

	private Socket clientSocket1,clientSocket2;
	protected State state;
	
	public GameServer(){
		
	}
	
	@Override
	public void run() {
		initializeGame();
		searchingForPlayers();
		playingGame();
		
		
	}
private void playingGame() {
		// TODO Själva spel logiken och main spel loopen
		
	}

private void searchingForPlayers() {
		// TODO Väntar på 2 spelare,
		
	}

private void initializeGame(){
	// TODO skapar serversocketen och datatstrukturer som behövs under spelets gång
}
private void addPlayer(Socket client2){
	clientSocket2 = client2;
	searchingForPlayers = false;
	
}
}
