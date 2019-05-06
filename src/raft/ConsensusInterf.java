package raft;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.RequestVotePar;
import entities.RequestVoteRes;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

/* implement the basic consensus algorithm */
public interface ConsensusInterf {
	public RequestVoteRes requestVote(RequestVotePar param);
	
	public AppendEntryRes appendEntries(AppendEntryPar param);
}
