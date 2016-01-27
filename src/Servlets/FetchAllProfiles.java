package Servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Server.Lobby;
import Server.GameServer;
import Server.State;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;

//TODO Sätt ihop strängen.



public class FetchAllProfiles extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ArrayList<GameServer> gameList;
		Lobby lobby = Lobby.getLobby();
		// TODO: send data to server
		gameList = lobby.fetchAllGames();
		
				
		// parsa ur datan vi vill ha ur denna
		String result="";
		for(int i =0;i<gameList.size();i++){
			String player1="-",player2="-";
			GameServer tempGame =gameList.get(i);
			
			if(tempGame.getState()==State.IDLE){
				player1=tempGame.getPlayers()[0].getUserName();
				player2 = "-";
			}
			else if(tempGame.getState() == State.FULL){
				player1 = tempGame.getPlayers()[0].getUserName();
				player2 = tempGame.getPlayers()[1].getUserName();
			}
			result = result +player1 + " " + player2 + " " + tempGame.getPortNumber() +" "+ tempGame.getState().toString()+" ";
		}
		response.getWriter().write(result);
	}
}