package Server;
import java.util.Arrays;

public class LobbyTest {

	public static void main(String[] args) {
		Lobby lobby = Lobby.getLobby();
		Player player = lobby.getPlayer("OneEyedPirate");
		//lobby.addWin("OneEyedPirate");
		//lobby.addLoss("OneEyedPirate");
		System.out.println("username: "+player.getUserName()+", password: "+player.getPassword()+
				", wins: "+player.getWins()+", losses: " + player.getLosses());
		String[] profile = lobby.fetchProfile("kukhuvve");
		
		String test = profile[0] +" "+profile[1]+" "+profile[2];
		System.out.println(test);
		System.out.println(Arrays.asList(profile));
		
	}

}
