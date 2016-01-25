package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameServer implements Runnable {

	private Socket clientSocket1, clientSocket2;
	private PrintWriter out1, out2;
	private BufferedReader in1, in2;
	private State state;
	private ServerSocket serverSocket;
	private int portNumber = 0;
	private Lobby lobby;
	private String[] board = new String[9];
	private ArrayList<Integer> player1Marks = new ArrayList<Integer>();
	private ArrayList<Integer> player2Marks = new ArrayList<Integer>();
	private ArrayList<PrintWriter> sendList = new ArrayList<PrintWriter>();
	private ArrayList<BufferedReader> receiveList = new ArrayList<BufferedReader>();
	// private ArrayList<Player> playerList = new ArrayList<Player>();
	private int[] scoreArray = new int[2];
	private Player[] playerArray = new Player[2];
	List<List<Integer>> wins = new ArrayList<List<Integer>>();
	Random rand = new Random();

	public GameServer(Lobby lobby) {
		Random rand = new Random();
		portNumber = rand.nextInt(1000) + 3000;
		this.lobby = lobby;
		state = State.NOT_READY;
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
		//getPlayerInfo();
		initializeGame();
		playGame();

	}
	private void getPlayer1Info(){
		String response;
		// boolean keepGoing = true;
		try {
			System.out.println("--------waiting for hello Player 1-----------");
			response = null;
			while (response == null) {
				response = in1.readLine();		
			}
			Player player1 = lobby.getPlayer(response);
			playerArray[0] = player1;
			out1.println("WAIT");
			out1.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void getPlayer2Info(){
		String response;
		// boolean keepGoing = true;
		try {
			System.out.println("--------waiting for hello Player 2-----------");
			response = null;
			while (response == null) {
				response = in2.readLine();		
			}
			Player player2 = lobby.getPlayer(response);
			playerArray[1] = player2;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getPlayerInfo() {
		String response;
		// boolean keepGoing = true;
		try {
			System.out.println("--------waiting for hello Player 1-----------");
			response = null;
			while (response == null) {
				response = in1.readLine();		
			}
			Player player1 = lobby.getPlayer(response);
			playerArray[0] = player1;
			System.out.println("--------waiting for hello Player 2-----------");
			while ((response = in2.readLine()) != null) {
				
				Player player2 = lobby.getPlayer(response);
				if (player2 == null) {
					System.out.println("getPlayerInfo(): could not find user " + response);
				}
				playerArray[1] = player2;
			}
			state = State.READY;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void playGame() {
		int startingPlayer = rand.nextInt(2); // starting player blir 0 eller 1
												// AKA player 1 eller 2
		int secondPlayer = 1 - startingPlayer; // är startingplayer 1(P2) blir
												// second 1-1 = 0(P1), annars
												// 1-0 =1(P2)
		boolean startingPlayerWon = false;
		// boolean startingPlayerWonGame = false;
		// skickar namnet på motståndaren till båda klienterna
		out1.println(playerArray[1].getUserName());
		out1.flush();
		out2.println(playerArray[0].getUserName());
		out2.flush();
		String startingMarker = "X";
		String secondMarker = "O";
		boolean keepGoing = true;
		boolean forfeit = false;
		
		while (true) { // hela gamet loop
			if (keepGoing) {
				if (startingPlayerWon) {
					int temp = startingPlayer;
					startingPlayer = secondPlayer;
					secondPlayer = temp;
					scoreArray = swapScore(scoreArray);
					playerArray = swapPlayers(playerArray);
				}
				System.out.println("--------Sending begin/wait-----------");
				sendList.get(startingPlayer).println("BEGIN");
				sendList.get(startingPlayer).flush();
				sendList.get(secondPlayer).println("WAIT");
				sendList.get(secondPlayer).flush();
			}

			while (keepGoing) { // loop för en omgång
				String response;

				try {
					while ((response = receiveList.get(startingPlayer).readLine()) != null) {
						// osäker på om den fortsätter till else, annars får jag
						// loopa på en boolean ist
						if (response == "forfeit") { // starting player ger upp,
														// så den förlorar och
														// den andra vinner
														// spelet
							// sen bryter vi hela loopen och spelet är klart
							forfeit = true;
							sendList.get(secondPlayer).println("WON GAME");
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println("LOST GAME");
							sendList.get(startingPlayer).flush();
							lobby.addWin(playerArray[secondPlayer].getUserName());
							lobby.addLoss(playerArray[startingPlayer].getUserName());
							break;
						}
						if (positionIsFree(Integer.parseInt(response))) {
							placeMarker(startingMarker, Integer.parseInt(response), startingPlayer);
							sendList.get(startingPlayer).println(startingMarker + "|" + response); // "X|position
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println(startingMarker + "|" + response);
							sendList.get(secondPlayer).flush();
							break;
						} else {
							sendList.get(startingPlayer).println("TAKEN");
							sendList.get(startingPlayer).flush();

						}
					}
					if (forfeit) {
						keepGoing = false;
						break;
					}
					if (checkForDraw()) {
						sendList.get(startingPlayer).println("DRAW");
						sendList.get(startingPlayer).flush();
						sendList.get(secondPlayer).println("DRAW");
						sendList.get(secondPlayer).flush();
						startingPlayerWon = true;
						break;
					}

					if (checkForWin(startingPlayer)) {
						scoreArray[startingPlayer] = scoreArray[startingPlayer] + 1;
						if (scoreArray[startingPlayer] == 3) {
							keepGoing = false;
							// startingPlayerWonGame = true;
							sendList.get(startingPlayer).println("WON GAME");
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println("LOST GAME");
							sendList.get(secondPlayer).flush();
							lobby.addWin(playerArray[startingPlayer].getUserName());
							lobby.addLoss(playerArray[secondPlayer].getUserName());
						} else {
							startingPlayerWon = true;
							sendList.get(startingPlayer).println("WON ROUND");
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println("LOST ROUND");
							sendList.get(secondPlayer).flush();

						}
						break;
					}
					sendList.get(secondPlayer).println("BEGIN");
					while ((response = receiveList.get(secondPlayer).readLine()) != null) {
						// osäker på om den fortsätter till else, annars får jag
						// loopa på en boolean ist

						if (response == "forfeit") { // starting player ger upp,
														// så den förlorar och
														// den andra vinner
														// spelet
							// sen bryter vi hela loopen och spelet är klart
							forfeit = true;
							sendList.get(startingPlayer).println("WON GAME");
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println("LOST GAME");
							sendList.get(secondPlayer).flush();
							lobby.addWin(playerArray[secondPlayer].getUserName());
							lobby.addLoss(playerArray[startingPlayer].getUserName());
							break;
						}
						if (positionIsFree(Integer.parseInt(response))) {
							placeMarker(secondMarker, Integer.parseInt(response), secondPlayer);
							sendList.get(secondPlayer).println(secondMarker + "|" + response); // "X|position
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println(secondMarker + "|" + response);
							sendList.get(startingPlayer).flush();
							break;
						} else {
							sendList.get(secondPlayer).println("TAKEN");
							sendList.get(secondPlayer).flush();
						}
					}

					if (forfeit) {
						keepGoing = false;
						break;
					}
					if (checkForDraw()) {
						sendList.get(startingPlayer).println("DRAW");
						sendList.get(startingPlayer).flush();
						sendList.get(secondPlayer).println("DRAW");
						sendList.get(secondPlayer).flush();
						startingPlayerWon = true;
						break;
					}

					if (checkForWin(secondPlayer)) {
						scoreArray[secondPlayer] = scoreArray[secondPlayer] + 1;
						if (scoreArray[secondPlayer] == 3) {
							keepGoing = false;
							sendList.get(secondPlayer).println("WON GAME");
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println("LOST GAME");
							sendList.get(startingPlayer).flush();
							lobby.addWin(playerArray[secondPlayer].getUserName());
							lobby.addLoss(playerArray[startingPlayer].getUserName());
						} else {

							sendList.get(secondPlayer).println("WON ROUND");
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println("LOST ROUND");
							sendList.get(startingPlayer).flush();

						}
						break;
					}
					sendList.get(startingPlayer).println("BEGIN");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!keepGoing) { // TODO här är osäkert läge, så kolla här först om
								// loopen buggar
				break;
			}
		}
		try {
			// TODO skulle kunna flytta dethär till nr jag detectat att någon
			// vunnit spelet
			clientSocket1.close();
			clientSocket2.close();
			serverSocket.close();
			lobby.removeGame(this.getPortNumber()); // denna måste imlementeras
			// TODO uppdatera wins o losses för players
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Player[] swapPlayers(Player[] players) {
		Player temp = players[0];
		players[0] = players[1];
		players[1] = temp;
		return players;
	}

	private int[] swapScore(int[] score) {
		int temp = score[0];
		score[0] = score[1];
		score[1] = temp;
		return score;
	}

	private boolean positionIsFree(int position) {
		if (!(player1Marks.contains(position)) && !(player2Marks.contains(position))) {
			return true;
		}
		return false;
	}

	private boolean checkForDraw() {
		for (int i = 0; i < board.length; i++) {
			if (board[i] == "-") {
				return false;
			}
		}
		return true;
	}

	private boolean checkForWin(int player) { // player är antingen 0 eller 1, 0
												// så kollar man player 1, 2 så
												// kollar man player 2

		// matchar varje siffra mot
		// player1Marks, stämmer alla har
		// man vunnit, gör detta för alla
		// row i wins
		if (player == 0) {
			for (int i = 0; i < wins.size(); i++) {
				if (player1Marks.contains(wins.get(i).get(0)) && player1Marks.contains(wins.get(i).get(1))
						&& player1Marks.contains(wins.get(i).get(2))) {
					return true;
				}

			}
			return false;
		} else {
			for (int i = 0; i < wins.size(); i++) {
				if (player2Marks.contains(wins.get(i).get(0)) && player2Marks.contains(wins.get(i).get(1))
						&& player2Marks.contains(wins.get(i).get(2))) {
					return true;
				}

			}
			return false;
		}
	}

	private void placeMarker(String marker, int position, int player) { // man
																		// får
																		// när
																		// man
																		// callar
																		// den
																		// hålla
																		// koll
																		// på
																		// vilken
																		// symbol
																		// och
																		// vilken
																		// spelare
		board[position] = marker;
		if (player == 0) {
			player1Marks.add(position); // dessa håller koll på varje spelares
										// positioner, och man kan använda
			// ArrayList.containsAll för att kolla mot de kombinationer som
			// betyder vinst.
		} else if (player == 1) {

			player2Marks.add(position);
		}
	}

	private void fillBoard() {
		for (int i = 0; i < board.length; i++) {
			board[i] = "-";
		}
	}

	public int getPortNumber() {
		return portNumber;
	}

	public State getState() {
		return state;
	}

	public Player[] getPlayers() {
		return playerArray;

	}

	private void searchingForPlayers() {
		// TODO här är first suspecr om det inte funkar som det ska. borde vara
		// lugnt eftersom serverSocket.accept
		// är blockerande men inte säker
		// boolean keepSearching = true;
		state = State.EMTPY;

		//while (keepSearching) {
			// if (!(clientSocket1.isBound()) && !(clientSocket2.isBound())) {
			try {
				clientSocket1 = serverSocket.accept();
				in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
				out1 = new PrintWriter(clientSocket1.getOutputStream());
				getPlayer1Info();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			state = State.IDLE;
			// } else if (clientSocket1.isBound() && !(clientSocket2.isBound()))
			// {
			try {
				clientSocket2 = serverSocket.accept();
				in2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
				out2 = new PrintWriter(clientSocket2.getOutputStream());
				getPlayer2Info();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//keepSearching = false;
			state = State.FULL;
			// }
		//}


	}

	private void initializeGame() {
		// TODO skapar serversocketen och datatstrukturer som behövs under
		// spelets gång
		wins.add(Arrays.asList(0, 1, 2));
		wins.add(Arrays.asList(3, 4, 5));
		wins.add(Arrays.asList(6, 7, 8));
		wins.add(Arrays.asList(0, 3, 6));
		wins.add(Arrays.asList(1, 4, 7));
		wins.add(Arrays.asList(2, 5, 8));
		wins.add(Arrays.asList(0, 4, 7));
		wins.add(Arrays.asList(2, 4, 6));
		sendList.add(out1);
		sendList.add(out2);
		receiveList.add(in1);
		receiveList.add(in2);
		scoreArray[0] = 0;
		scoreArray[1] = 0;
	}
}
