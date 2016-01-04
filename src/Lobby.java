import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lobby {

	ArrayList<GameServer> gameList = new ArrayList<GameServer>();
	ArrayList<Player> playerList = new ArrayList<Player>();
	int poolSize = 3;
	int portNumber = 1337;
	ExecutorService executor;

	public Lobby() {
		startExecutor();
	}

	private void startExecutor() {
		executor = Executors.newFixedThreadPool(poolSize);
	}

	public void createGame() {
		GameServer tempGame = new GameServer(this);
		gameList.add(tempGame);
		executor.execute(tempGame);
		
		
		// TODO Kolla upp vilken port gamet fick och skicka tillbaka det till
		// servleten så att den kan ge portnummer till clienten, kan skicka tillbaka det genom att returna
		//en int 
	}

	public boolean userExists(String userName) {		// kanske är onödig
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).equals(userName)) {
				return true;
			}
		}
		return false;
	}

	public Player getUser(String userName) {
		for (int i = 0; i < gameList.size(); i++) {
			if (playerList.get(i).equals(userName)) {
				return playerList.get(i);
			}
		}
		return null;
	}
}
