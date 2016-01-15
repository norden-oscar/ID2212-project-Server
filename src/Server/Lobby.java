package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO Kvar att göra här: Implementera removeGame, som ska kallas från en GameServer när den e klar så att den tas
//bort från listan med games osv


public class Lobby {

	ArrayList<GameServer> gameList = new ArrayList<GameServer>();
	ArrayList<Player> registeredPlayers = new ArrayList<Player>();
	//HashMap<Integer,ArrayList<String>> playersInGame =  new HashMap<Integer,ArrayList<String>>();
	int poolSize = 30;
	int portNumber = 1337;
	ExecutorService executor;
	private Connection conn;
	private PreparedStatement register;
	private PreparedStatement fetchPlayers;
	private PreparedStatement updateWins;
	private PreparedStatement updateLosses;
	private String datasource = "game_db";

	private static Lobby instance = null;

	public static Lobby getLobby() {
		if (instance == null) {
			instance = new Lobby();
		}
		return instance;
	}

	protected Lobby() {

		startExecutor();
		connectSQL();
		fetchPlayers();
		try {
			register = conn.prepareStatement("INSERT INTO players (username, password) VALUES (?, ?)");
			updateWins = conn.prepareStatement("UPDATE players SET wins = ? WHERE username = ?");
			updateLosses = conn.prepareStatement("UPDATE players SET losses = ? WHERE username = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addWin(String userName) {
		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).userName.equals(userName)) {
				registeredPlayers.get(i).addWin();
				int wins = registeredPlayers.get(i).getWins();
				try {
					updateWins.setInt(1, wins);
					updateWins.setString(2, userName);
					updateWins.executeUpdate();

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		// System.out.println("lobby.addWin: something went wrong with finding
		// player");

	}

	public void addLoss(String userName) {
		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).userName.equals(userName)) {
				registeredPlayers.get(i).addLoss();
				int losses = registeredPlayers.get(i).getLosses();
				try {
					updateLosses.setInt(1, losses);
					updateLosses.setString(2, userName);
					updateLosses.executeUpdate();

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		System.out.println("lobby.addLoss: something went wrong with finding player");

	}

	public ArrayList<GameServer> fetchAllGames(){
		return gameList;
	}
	public Player getPlayer(String userName) {
		for (int i = 0; i < registeredPlayers.size(); i++) {
			Player tempPlayer = registeredPlayers.get(i);
			if (tempPlayer.equals(userName)) {
				return tempPlayer;
			}
		}
		return null;
	}

	public String findGame(String userName) {
		for (int i = 0; i < gameList.size(); i++) {
			if (gameList.get(i).getState() == State.EMTPY || gameList.get(i).getState() == State.IDLE) {
				int port=gameList.get(i).getPortNumber();
				//playersInGame.put(port,userName);
				return "" + port;
				
			}
		}
		GameServer newGame = createGame();
		while (true) {
			if (newGame.getState() == State.EMTPY || newGame.getState() == State.IDLE) {
				
				return "" + newGame.getPortNumber();
			}
		}

	}

	public String[] fetchProfile(String userName) {
		Player player;
		String[] answer = new String[3];
		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).getUserName().equals(userName)) {
				player = registeredPlayers.get(i);
				answer[0] = player.getUserName();
				answer[1] = "" + player.getWins();
				answer[2] = "" + player.getLosses();
				return answer;
			}
		}
		answer[0] = "NO_SUCH_USER_EXISTS";
		return answer;
	}

	public void fetchPlayers() {
		try {
			fetchPlayers = conn.prepareStatement("SELECT * FROM players");

			ResultSet rs1 = fetchPlayers.executeQuery();
			System.out.println("Registered users: ");
			while (rs1.next()) {

				String loginUserName = rs1.getString(1);
				String loginPassWord = rs1.getString(2);
				int wins = rs1.getInt(3);
				int losses = rs1.getInt(4);
				Player player = new Player(loginUserName, loginPassWord, wins, losses);
				registeredPlayers.add(player);
				System.out.println(player.getUserName());

				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String loginUser(String userName, String passWord) {
		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).getUserName().equals(userName)) {
				if (registeredPlayers.get(i).getPassword().equals(passWord)) {
					return "OK";
				}
				return "WRONG_PASSWORD";
			}
		}
		return "NO_SUCH_USER_EXISTS";

	}

	public String registerUser(String userName, String passWord) {

		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).getUserName().equals(userName)) {
				return "USER_ALREADY_EXISTS";
			}
		}
		try {
			register.setString(1, userName);
			register.setString(2, passWord);
			register.executeUpdate();
			clearParameters();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "COULD_NOT_REGISTER";
		}
		return "OK";

	}

	private void clearParameters() {
		try {
			register.clearParameters();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connectSQL() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + datasource, "root", "root");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Connected to database..." + conn.toString());
	}

	private void startExecutor() {
		executor = Executors.newFixedThreadPool(poolSize);
	}

	private GameServer createGame() {
		GameServer tempGame = new GameServer(this);
		gameList.add(tempGame);
		executor.execute(tempGame);
		return tempGame;

		// TODO Kolla upp vilken port gamet fick och skicka tillbaka det till
		// servleten så att den kan ge portnummer till clienten, kan skicka
		// tillbaka det genom att returna
		// en int
	}

	public void removeGame(int portNumber) {
		for(int i; i<gameList.size();i++){
			if(gameList.get(i).getPortNumber()==portNumber){
				gameList.remove(i);
				break;
			}
		}
		
	}

}
