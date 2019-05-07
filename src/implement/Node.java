package implement;

import java.util.HashMap;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.Log;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import entities.Status;
import raft.NodeInterf;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Node implements NodeInterf {
	
	private int status = Status.FOLLOWER;
	private String leaderID;
	
	public Node() {
		log = new Log();
		new Consensus(this);
	}
	
	public void setStatus(int s) {
		status = s;
	}
	
	public void setLeader(String l) {
		leaderID = l;
	}
	
	/** Persistent state on all servers
	  * Updated on stable storage before 
	  * responding to RPCs
	  */
	private int currentTerm = 0; // latest term server has seen
	private String votedFor; // candidateId that received vote in current term 
	private Log log; //// log entries
	
	// Volatile state on all servers
	private int commitIndex; // index of highest log entry known to be committed 
	int lastApplied; // index of highest log entry applied to state machine 
	
	// Volatile state on leaders
	HashMap<Node,Integer> nextIndex; // for each server, index of the next log entry to send to that server
	HashMap<Node,Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server
	
	public int getCurrentTerm() {
		return currentTerm;
	}
	
	public void setCurrentTerm(int ct) {
		currentTerm = ct;
	}
	
	public String getVotedFor() {
		return votedFor;
	}
	
	public void setVotedFor(String vf) {
		votedFor = vf;
	}
	
	public int lastLogIndex() {
		return log.getLastLogIndex();
	}
	
	public int lastLogTerm() {
		return log.getLastLogTerm();
	}
	
	public int EntryTerm(int index) {
		return log.getEntryTerm(index);
	}
	
	public String EntryCommand(int index) {
		return log.getCommand(index);
	}
	
	public int getCommitIndex() {
		return commitIndex;
	}
	
	@Override
	public RequestVoteRes rVoteOperator(RequestVotePar param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppendEntryRes aEntriesOperator(AppendEntryPar param) {
		// TODO Auto-generated method stub
		return null;
	}

}
