package raft;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.RequestVotePar;
import entities.RequestVoteRes;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

/* each server is treated as a node
 * implement the operators for 
 * requestvote RPC and appendEntry RPC */

public interface NodeInterf {	
	public RequestVoteRes rVoteOperator(RequestVotePar param);
	
	public AppendEntryRes aEntriesOperator(AppendEntryPar param);
}
