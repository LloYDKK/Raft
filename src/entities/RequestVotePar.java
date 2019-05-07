package entities;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class RequestVotePar {
	private int term; //candidate's term
	private String candidateId; //candidate requesting vote
	private int lastLogIndex; // index of candidate's last log entry
	private int lastLogTerm; //term of candidate's last log entry
	
	private RequestVotePar(Builder builder) {
		this.term = builder.term;
		this.candidateId = builder.candidateId;
		this.lastLogIndex = builder.lastLogIndex;
		this.lastLogTerm = builder.lastLogTerm;
	}
	
	public int getTerm() {
		return term;
	}
	
	public String getCandidate() {
		return candidateId;
	}
	
	public int getLastLogIndex() {
		return lastLogIndex;
	}
	
	public int getlastLogTerm() {
		return lastLogTerm;
	}
	
	public static final class Builder{
		private int term;
		private String candidateId;
		private int lastLogIndex;
		private int lastLogTerm;
		
		public Builder term(int t) {
			term = t;
			return this;
		}
		
		public Builder candidateId(String t) {
			candidateId = t;
			return this;
		}
		
		public Builder lastLogIndex(int t) {
			lastLogIndex = t;
			return this;
		}
		
		public Builder lastLogTerm(int t) {
			lastLogTerm = t;
			return this;
		}
		
		public RequestVotePar build() {
			return new RequestVotePar(this);
		}
	}
}
