import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lobby {

	ArrayList<GameServer> gameList = new ArrayList<GameServer>();
	ArrayList<Player> registeredPlayers = new ArrayList<Player>();

	int poolSize = 3;
	int portNumber = 1337;
	ExecutorService executor;
	private Connection conn;
	private PreparedStatement register;
	private PreparedStatement fetchPlayers;
	private String datasource = "game_db";

	public Lobby() {
		startExecutor();
		connectSQL();
		fetchPlayers();
		try {
			register = conn.prepareStatement("INSERT INTO players (username, password) VALUES (?, ?)");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String[] fetchProfile(String userName) {
		Player player;
		String[] answer = new String[3];
		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).getUserName().equals(userName)) {
				player = registeredPlayers.get(i);
				answer[0]=player.getUserName();
				answer[1]=""+player.getWins();
				answer[2]=""+player.getLosses();
				return answer;
			}
		}
		answer[0]= "NO_SUCH_USER_EXISTS";
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

				// trader = new TraderImpl(userame, passWord, itemsSold,
				// itemBought);
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
					return "SUCESS";
				}
				return "WRONG_PASSWORD";
			}
		}
		return "NO_SUCH_USER_EXISTS";

	}

	public String registerUser(String userName, String passWord) {

		for (int i = 0; i < registeredPlayers.size(); i++) {
			if (registeredPlayers.get(i).getUserName().equals(userName)) {
				return "USER_ALLREADY_EXISTS";
			}
		}
		try {
			register.setString(1, userName);
			register.setString(2, passWord);
			register.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "COULD_NOT_REGISTER";
		}
		return "SUCESS";

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

	public void createGame() {
		GameServer tempGame = new GameServer(this);
		gameList.add(tempGame);
		executor.execute(tempGame);

		// TODO Kolla upp vilken port gamet fick och skicka tillbaka det till
		// servleten så att den kan ge portnummer till clienten, kan skicka
		// tillbaka det genom att returna
		// en int
	}

}
