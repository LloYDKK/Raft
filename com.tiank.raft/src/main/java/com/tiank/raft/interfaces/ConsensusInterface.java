package com.tiank.raft.interfaces;

import com.tiank.raft.entities.AppendEntryPar;
import com.tiank.raft.entities.AppendEntryRes;
import com.tiank.raft.entities.RequestVotePar;
import com.tiank.raft.entities.RequestVoteRes;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Kuan Tian
 * 2019-05-04
 */
/* implement the basic consensus algorithm */
public interface ConsensusInterface extends Remote {
    // the RequestVote RPC as described in the report
    RequestVoteRes requestVote(RequestVotePar param) throws RemoteException;

    // the AppendEntries RPC as described in the report
    AppendEntryRes appendEntries(AppendEntryPar param) throws RemoteException;

    // the AddPeer RPC that used by new peers to join the cluster
    String addPeer(String address) throws RemoteException;
}
