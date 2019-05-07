package implement;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import entities.Status;
import raft.ConsensusInterf;
import raft.NodeInterf;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Consensus implements ConsensusInterf {
	private final Node node;
	
	public Consensus(Node n) {
		node = n;
	}
	
	@Override
	public RequestVoteRes requestVote(RequestVotePar param) {
		// TODO Auto-generated method stub
		
		// unpacking the arguements
		int candidTerm = param.getTerm();
		String candidID = param.getCandidate();
		int candidLogIndex = param.getLastLogIndex();
		int candidLogTerm = param.getlastLogTerm();
		
		// status for the current node
		int currentTerm = node.getCurrentTerm();
		int nodeLogIndex = node.lastLogIndex();
		int nodeLogTerm = node.lastLogTerm();
		
		// Reply false if term < currentTerm (¡ì5.1)
		if (candidTerm < currentTerm) return new RequestVoteRes(currentTerm,false);
		
		/** If votedFor is null or candidateId, and candidate¡¯s log is at
	      * least as up-to-date as receiver¡¯s log, grant vote (¡ì5.2, ¡ì5.4)
	      */ 
		if ((node.getVotedFor().equals("") || node.getVotedFor().equals(candidID))
			 && nodeLogIndex <= candidLogIndex
		     && nodeLogTerm <= candidLogTerm){
			node.setStatus(Status.FOLLOWER);
			node.setLeader(candidID);
			node.setVotedFor(candidID);
			node.setCurrentTerm(candidTerm);
			return new RequestVoteRes(candidTerm,true);
		}
		
		return new RequestVoteRes(currentTerm,false);
	}

	@Override
	public AppendEntryRes appendEntries(AppendEntryPar param) {
		// TODO Auto-generated method stub
		
		// unpacking the arguments
		int leaderTerm = param.getTerm();
		int prevLogIndex = param.getPreLogIndex();
		int prevLogTerm = param.getPreLogTerm();
		String entries = param.getEntry();
		
		int currentTerm = node.getCurrentTerm();
		
		// Reply false if term < currentTerm (¡ì5.1)
		if(leaderTerm<currentTerm) return new AppendEntryRes(currentTerm,false);
		
		// Reply false if log doesn¡¯t contain an entry at prevLogIndex whose term matches prevLogTerm
		if(node.logEntryTerm(prevLogIndex)!=prevLogTerm) return new AppendEntryRes(currentTerm,false);
		
		/** If an existing entry conflicts with a new one (same index
		  * but different terms), delete the existing entry and all that
		  * follow it (¡ì5.3)
          */
		
		return null;
	}

}
