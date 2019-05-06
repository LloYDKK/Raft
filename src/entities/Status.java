package entities;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

/* the status of the nodes */
public class Status {
	private final int FOLLOWER = 0;
	private final int CANDIDATE = 1;
	private final int LEADER = 2;
	
	public int follower() {
		return FOLLOWER;
	}
	
	public int candiate() {
		return CANDIDATE;
	}
	
	public int leader() {
		return LEADER;
	}
}
