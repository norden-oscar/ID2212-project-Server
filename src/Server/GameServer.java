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
		getPlayerInfo();
		initializeGame();
		playGame();

	}

	private void getPlayerInfo() {
		String response;
		// boolean keepGoing = true;
		try {
			while ((response = in1.readLine()) != null) {
				Player player = lobby.getPlayer(response);
				if (player == null) {
					System.out.println("getPlayerInfo(): could not find user " + response);
				}
				playerArray[0] = player;
			}
			while((response = in2.readLine()) != null){
				Player player = lobby.getPlayer(response);
				if (player == null) {
					System.out.println("getPlayerInfo(): could not find user " + response);
				}
				playerArray[1] = player;
			}
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
		String startingMarker = "X";
		String secondMarker = "O";
		boolean keepGoing = true;
		while (true) { // hela gamet loop
			if (keepGoing) {
				if (startingPlayerWon) {
					int temp = startingPlayer;
					startingPlayer = secondPlayer;
					secondPlayer = temp;
					scoreArray = swapScore(scoreArray);
					playerArray = swapPlayers(playerArray);
				}
				sendList.get(startingPlayer).println("begin");
				sendList.get(startingPlayer).flush();
				sendList.get(secondPlayer).println("wait");
				sendList.get(secondPlayer).flush();
			}

			while (keepGoing) { // loop för en omgång
				String response;

				try {
					while ((response = receiveList.get(startingPlayer).readLine()) != null) {
						// osäker på om den fortsätter till else, annars får jag
						// loopa på en boolean ist

						if (positionIsFree(Integer.parseInt(response))) {
							placeMarker(startingMarker, Integer.parseInt(response), startingPlayer);
							sendList.get(startingPlayer).println(startingMarker + "|" + response); // "X|position
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println(startingMarker + "|" + response);
							sendList.get(secondPlayer).flush();
							break;
						} else {
							sendList.get(startingPlayer).println("taken");
						}
					}
					if (checkForWin(startingPlayer)) {
						scoreArray[startingPlayer] = scoreArray[startingPlayer] + 1;
						if (scoreArray[startingPlayer] == 3) {
							keepGoing = false;
							startingPlayerWon = true;
							sendList.get(startingPlayer).println("won game");
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println("lost game");
							sendList.get(secondPlayer).flush();
						} else {

							sendList.get(startingPlayer).println("won round");
							sendList.get(startingPlayer).flush();
							sendList.get(secondPlayer).println("lost round");
							sendList.get(secondPlayer).flush();

						}
						break;
					}

					while ((response = receiveList.get(secondPlayer).readLine()) != null) {
						// osäker på om den fortsätter till else, annars får jag
						// loopa på en boolean ist

						if (positionIsFree(Integer.parseInt(response))) {
							placeMarker(secondMarker, Integer.parseInt(response), secondPlayer);
							sendList.get(secondPlayer).println(secondMarker + "|" + response); // "X|position
							sendList.get(startingPlayer).println(secondMarker + "|" + response);
							break;
						} else {
							sendList.get(secondPlayer).println("taken");
						}
					}
					if (checkForWin(secondPlayer)) {
						scoreArray[secondPlayer] = scoreArray[secondPlayer] + 1;
						if (scoreArray[secondPlayer] == 3) {
							keepGoing = false;
							sendList.get(secondPlayer).println("won game");
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println("lost game");
							sendList.get(startingPlayer).flush();
						} else {

							sendList.get(secondPlayer).println("won round");
							sendList.get(secondPlayer).flush();
							sendList.get(startingPlayer).println("lost round");
							sendList.get(startingPlayer).flush();

						}
						break;
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
		try {

			clientSocket1.close();
			clientSocket2.close();
			serverSocket.close();
			// TODO uppdatera wins o losses för players
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private Player[] swapPlayers(Player[] players){
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

	private void searchingForPlayers() {
		boolean keepSearching = true;
		state = State.EMTPY;
		// TODO Väntar på 2 spelare,
		while (keepSearching) {
			// if (!(clientSocket1.isBound()) && !(clientSocket2.isBound())) {
			try {
				clientSocket1 = serverSocket.accept();
				in1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
				out1 = new PrintWriter(clientSocket1.getOutputStream());

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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keepSearching = false;
			state = State.FULL;
			// }
		}

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
