
public class Player {

	String userName;
	int wins, losses;
	float rank;

	public Player(String userName, int wins, int losses) {
		this.userName = userName;
		this.wins = wins;
		this.losses = losses;
		this.rank = calculateRank(wins, losses);
	}

	private float calculateRank(int wins, int losses) {
		int rank;
		if (wins == 0) {
			rank = -losses;
		} else if (losses == 0) {
			rank = wins;
		} else {
			rank = wins / losses;
		}
		return rank;

	}

	public String getUserName() {
		return userName;
	}

	public float getRank() {
		return rank;
	}

	public void addWin() {
		wins = wins + 1;
		rank = calculateRank(wins,losses);
	}
	public void addLoss(){
		losses = losses+1;
		rank = calculateRank(wins, losses);
	}
}
