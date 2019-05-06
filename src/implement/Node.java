package implement;

import java.util.HashMap;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.Log;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import raft.NodeInterf;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Node implements NodeInterf {
	
	/** Persistent state on all servers
	  * Updated on stable storage before 
	  * responding to RPCs
	  */
	int currentTerm = 0; // latest term server has seen
	String votedFor; // candidateId that received vote in current term 
	Log[] log; // log entries
	
	// Volatile state on all servers
	int commitIndex; // index of highest log entry known to be committed 
	int lastApplied; // index of highest log entry applied to state machine 
	
	// Volatile state on leaders
	HashMap<Node,Integer> nextIndex;
	HashMap<Node,Integer> matchIndex;
	
	
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
