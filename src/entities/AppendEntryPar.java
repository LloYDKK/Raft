package entities;

import java.io.Serializable;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class AppendEntryPar implements Serializable{
	private int term; // leader's term
	private String leaderId; // leader's ID
	private int preLogIndex; // index of log entry immediately preceding new ones
	private int preLogTerm; // term of prevLogIndex entry
	private Entry[] entries; // log entries(empty for heartbeat)
	private int leaderCommit; // leader's commitIndex
	private PeerList peerList; // store the addresses of all the peers, used to renew the peerlist on each peers
	
	public int getTerm() {
		return term;
	}
	
	public int getPreLogIndex() {
		return preLogIndex;
	}
	
	public int getPreLogTerm() {
		return preLogTerm;
	}
	
	public Entry[] getEntry() {
		return entries;
	}
	
	public int getLeaderCommit() {
		return leaderCommit;
	}
	
	public PeerList getPeerList() {
		return peerList;
	}
	
	// implement a builder pattern for this class
	// to make the init process more straight forward
	private AppendEntryPar(Builder builder) {
		this.term = builder.term;
		this.leaderId = builder.leaderId;
		this.preLogIndex = builder.preLogIndex;
		this.preLogTerm = builder.preLogTerm;
		this.entries = builder.entries;
		this.leaderCommit = builder.leaderCommit;
		this.peerList = builder.peerList;
	}
	
	public static final class Builder{
		private int term;
		private String leaderId;
		private int preLogIndex;
		private int preLogTerm;
		private Entry[] entries;
		private int leaderCommit;
		private PeerList peerList;
		
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
		
		public Builder entries(Entry[] t) {
			entries = t;
			return this;
		}
		
		public Builder leaderCommit(int t) {
			leaderCommit = t;
			return this;
		}
		
		public Builder peerList(PeerList t) {
			peerList = t;
			return this;
		}
		
		public AppendEntryPar build() {
			return new AppendEntryPar(this);
		}
	}
}
