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
		// TODO Sj�lva spel logiken och main spel loopen
		
	}

private void searchingForPlayers() {
		// TODO V�ntar p� 2 spelare,
		
	}

private void initializeGame(){
	// TODO skapar serversocketen och datatstrukturer som beh�vs under spelets g�ng
}
private void addPlayer(Socket client2){
	clientSocket2 = client2;
	searchingForPlayers = false;
	
}
}
