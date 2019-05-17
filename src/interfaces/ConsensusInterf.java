package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.RequestVotePar;
import entities.RequestVoteRes;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

/* implement the basic consensus algorithm */
public interface ConsensusInterf extends Remote{
	// the request vote RPC as described in the report
	public RequestVoteRes requestVote(RequestVotePar param) throws RemoteException;
	
	// the append entries RPC as described in the report
	public AppendEntryRes appendEntries(AppendEntryPar param) throws RemoteException;
	
	// the add peer RPC that used by new peers to join the cluster
	public String addPeer(String address) throws RemoteException;
}
