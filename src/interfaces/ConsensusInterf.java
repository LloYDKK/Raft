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
	public RequestVoteRes requestVote(RequestVotePar param) throws RemoteException;
	
	public AppendEntryRes appendEntries(AppendEntryPar param) throws RemoteException;
}
