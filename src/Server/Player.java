package Server;

public class Player {

	String userName;
	String passWord;
	int wins, losses;
	


	public Player(String userName,String passWord, int wins, int losses) {
		this.userName = userName;
		this.passWord = passWord;
		this.wins = wins;
		this.losses = losses;
		
	}


	public int getWins() {
		return wins;
	}


	public int getLosses() {
		return losses;
	}

	
	public String getPassword(){
		return passWord;
	}
	
	public String getUserName() {
		return userName;
	}

	

	public void addWin() {
		wins = wins + 1;
		
	}
	public void addLoss(){
		losses = losses+1;
		
	}
			//Player.equals används inte atm
	public boolean equals(String userName){
		if(this.userName.equals(userName)){
			return true;
		}
		else return false;
	}
}
