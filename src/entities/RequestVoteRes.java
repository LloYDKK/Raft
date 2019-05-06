package entities;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class RequestVoteRes {
	private int term;  // currentTerm, for candidate to update itself
	private boolean voteGranted;  // true means candidate received vote
	
	public RequestVoteRes(int t, boolean v) {
		this.term = t;
		this.voteGranted = v;
	}
	
	public RequestVoteRes(int t) {
		this.term = t;
	}
	
	public RequestVoteRes(boolean v) {
		this.voteGranted = v;
	}
}
