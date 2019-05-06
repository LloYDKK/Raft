package implement;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import raft.ConsensusInterf;
import raft.NodeInterf;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Consensus implements ConsensusInterf {
	public final Node node;
	
	public Consensus(Node n) {
		this.node = n;
	}
	
	@Override
	public RequestVoteRes requestVote(RequestVotePar param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AppendEntryRes appendEntries(AppendEntryPar param) {
		// TODO Auto-generated method stub
		return null;
	}

}
