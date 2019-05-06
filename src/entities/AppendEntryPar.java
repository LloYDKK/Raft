package entities;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class AppendEntryPar {
	private int term; // leader's term
	private String leaderId; // leader's ID
	private int preLogIndex; // index of log entry immediately preceding new ones
	private int preLogTerm; // term of prevLogIndex entry
	private Log[] entries; // log entries(empty for heartbeat)
	private int leaderCommit; //leader's commitIndex
	
	private AppendEntryPar(Builder builder) {
		this.term = builder.term;
		this.leaderId = builder.leaderId;
		this.preLogIndex = builder.preLogIndex;
		this.preLogTerm = builder.preLogTerm;
		this.entries = builder.entries;
		this.leaderCommit = builder.leaderCommit;
	}
	
	public static final class Builder{
		private int term;
		private String leaderId;
		private int preLogIndex;
		private int preLogTerm;
		private Log[] entries;
		private int leaderCommit;
		
		public Builder term(int t) {
			term = t;
			return this;
		}
		
		public Builder leaderId(String t) {
			leaderId = t;
			return this;
		}
		
		public Builder preLogIndex(int t) {
			preLogIndex = t;
			return this;
		}
		
		public Builder preLogTerm(int t) {
			preLogTerm = t;
			return this;
		}
		
		public Builder entries(Log[] t) {
			entries = t;
			return this;
		}
		
		public Builder leaderCommit(int t) {
			leaderCommit = t;
			return this;
		}
		
		public AppendEntryPar build() {
			return new AppendEntryPar(this);
		}
	}
}