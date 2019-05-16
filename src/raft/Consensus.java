package raft;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.Entry;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import entities.Status;
import interfaces.ConsensusInterf;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Consensus extends UnicastRemoteObject implements ConsensusInterf {
	private final Node node;
	private static Logger LOG = Logger.getAnonymousLogger();
	
	protected Consensus(Node n) throws RemoteException {
		node = n;
	}
	
	@Override
	public RequestVoteRes requestVote(RequestVotePar param) throws RemoteException {
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
		node.receiveFromLeader();
		
		// Reply false if term < currentTerm (¡ì5.1)
		if (candidTerm < currentTerm) {
			LOG.info("Election: term < currentTerm!");
			return new RequestVoteRes(currentTerm,false);
		}
		
		/** If votedFor is null or candidateId, and candidate¡¯s log is at
	      * least as up-to-date as receiver¡¯s log, grant vote (¡ì5.2, ¡ì5.4)
	      */ 
		if ((node.getVotedFor().equals("") || node.getVotedFor().equals(candidID))
			 && nodeLogIndex <= candidLogIndex
		     && nodeLogTerm <= candidLogTerm){
			LOG.info("vote for: "+ candidID);
			node.setStatus(Status.FOLLOWER);
			node.setLeader(candidID);
			node.setVotedFor(candidID);
			node.setCurrentTerm(candidTerm);
			return new RequestVoteRes(candidTerm,true);
		}
		
		return new RequestVoteRes(currentTerm,false);
	}

	@Override
	public AppendEntryRes appendEntries(AppendEntryPar param) throws RemoteException {
		// TODO Auto-generated method stub
		
		// unpacking the arguments
		int leaderTerm = param.getTerm();
		int prevLogIndex = param.getPreLogIndex();
		int prevLogTerm = param.getPreLogTerm();
		Entry[] entries = param.getEntry();
		int leaderCommit = param.getLeaderCommit();
		
		// status for the current node
		int currentTerm = node.getCurrentTerm();
		node.receiveFromLeader();
		
		// Reply false if term < currentTerm (¡ì5.1)
		if(leaderTerm<currentTerm) {
			LOG.info("Append Entry: term < currentTerm!");
			return new AppendEntryRes(currentTerm,false);
		}
		
		// if term > currentTerm, set the follower status 
		if(leaderTerm>=currentTerm) {
			node.setStatus(Status.FOLLOWER);
			node.setCurrentTerm(leaderTerm);
		}
		
		// receive heartbeat
		if(entries.length == 0) {
			LOG.info("receive heartbeat from leader");
			return new AppendEntryRes(currentTerm,true);
		}
		
		// Reply false if log doesn¡¯t contain an entry at prevLogIndex whose term matches prevLogTerm
		if(node.logEntryTerm(prevLogIndex) != -1 && node.logEntryTerm(prevLogIndex)!=prevLogTerm) {
			LOG.info("log doesn¡¯t contain an entry at prevLogIndex!");
			return new AppendEntryRes(currentTerm,false);
		}
		
		/** If an existing entry conflicts with a new one (same index
		  * but different terms), delete the existing entry and all that
          * follow it (¡ì5.3)
          */
		if(node.logEntryTerm(prevLogIndex+1) != -1
				&& node.logEntryTerm(prevLogIndex+1) != param.getEntry()[0].getEntryTerm()) {
			node.logDeleteFrom(prevLogIndex+1);
			
		}
		
		// Append any new entries not already in the log
		for(Entry e:entries){
			node.addEntry(e);
			}
		
		if(leaderCommit > node.getCommitIndex()) {
			int i = (int) Math.min(leaderCommit, node.lastLogIndex());
			node.setCommitIndex(i);
		}
		
		// apply the command to the state machine if lastApplied < commitIndex
		if(node.getCommitIndex()>node.getLastApplied()) {
			Thread t1 = new Thread();
			try {
				t1 = new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						for(int i = node.getLastApplied();i<=node.getCommitIndex();i++) {
							node.executeStateMachine(node.logEntryCommand(i));
						}
					}
					
				});
				t1.start();
			}finally {
				t1.interrupt();
			}
		}
		
		return new AppendEntryRes(currentTerm,true);
	}
	
}
