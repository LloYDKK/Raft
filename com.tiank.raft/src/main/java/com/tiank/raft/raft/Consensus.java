package com.tiank.raft.raft;

import com.tiank.raft.entities.AppendEntryPar;
import com.tiank.raft.entities.AppendEntryRes;
import com.tiank.raft.entities.Entry;
import com.tiank.raft.entities.PeerList;
import com.tiank.raft.entities.RequestVotePar;
import com.tiank.raft.entities.RequestVoteRes;
import com.tiank.raft.entities.Status;
import com.tiank.raft.interfaces.ConsensusInterface;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * @author Kuan Tian
 * 2019-05-06
 */
public class Consensus extends UnicastRemoteObject implements ConsensusInterface {
    private static final Logger LOG = Logger.getAnonymousLogger();
    public final ReentrantLock voteLock = new ReentrantLock();
    public final ReentrantLock appendLock = new ReentrantLock();
    private final Node node;

    protected Consensus(Node n) throws RemoteException {
        node = n;
    }

    @Override
    public RequestVoteRes requestVote(RequestVotePar param) throws RemoteException {
        // TODO Auto-generated method stub
        try {

            // unpacking the arguments
            int candidTerm = param.getTerm();
            String candidID = param.getCandidate();
            int candidLogIndex = param.getLastLogIndex();
            int candidLogTerm = param.getlastLogTerm();

            // status for the current node
            int currentTerm = node.getCurrentTerm();
            int nodeLogIndex = node.lastLogIndex();
            int nodeLogTerm = node.lastLogTerm();
            node.receiveFromLeader();

            LOG.info(node.getName() + ": Receive Election Message from " + candidID);

            if (!voteLock.tryLock()) {
                LOG.info("Lock");
                return new RequestVoteRes(currentTerm, false);
            }

            // Reply false if term < currentTerm
            if (candidTerm < currentTerm) {
                LOG.info(node.getName() + ": Respond Election: term < currentTerm!");
                return new RequestVoteRes(currentTerm, false);
            }

            // reply false if the node is the leader
            if (node.isLeader()) {
                LOG.info(node.getName() + ": Respond Election: Abort Election!");
                return new RequestVoteRes(currentTerm, false);
            }

            /** If votedFor is null or candidateId, and candidate��s log is at
             * least as up-to-date as receiver��s log, grant vote
             */
            if ((node.getVotedFor().equals("") || node.getVotedFor().equals(candidID))
                    && nodeLogIndex <= candidLogIndex
                    && nodeLogTerm <= candidLogTerm) {

                LOG.info(node.getName() + ": vote for " + candidID);
                node.setStatus(Status.FOLLOWER);
                node.setLeader(candidID);
                node.setVotedFor(candidID);
                node.setCurrentTerm(candidTerm);
                return new RequestVoteRes(candidTerm, true);
            }

            return new RequestVoteRes(currentTerm, false);
        } finally {
            voteLock.unlock();
        }
    }

    @Override
    public AppendEntryRes appendEntries(AppendEntryPar param) throws RemoteException {
        // TODO Auto-generated method stub
        try {
            // unpacking the arguments
            int leaderTerm = param.getTerm();
            int prevLogIndex = param.getPreLogIndex();
            int prevLogTerm = param.getPreLogTerm();
            Entry[] entries = param.getEntry();
            int leaderCommit = param.getLeaderCommit();
            PeerList peerList = param.getPeerList();

            // status for the current node
            int currentTerm = node.getCurrentTerm();
            node.receiveFromLeader();

            if (!appendLock.tryLock()) {
                return new AppendEntryRes(currentTerm, false);
            }

            // Reply false if term < currentTerm
            if (leaderTerm < currentTerm) {
                LOG.info(node.getName() + ": Respond Append Entry: term < currentTerm!");
                return new AppendEntryRes(currentTerm, false);
            }

            // add new peer
            if (peerList.peerAmount() != node.getPeerAmount()) {
                node.setPeerList(peerList);
            }

            // if term > currentTerm, set the follower status
            node.setStatus(Status.FOLLOWER);
            node.setCurrentTerm(leaderTerm);

            // receive heartbeat
            if (entries.length == 0) {
                LOG.info(node.getName() + ": receive heartbeat from leader");
                return new AppendEntryRes(currentTerm, true);
            }

            // Reply false if log doesn��t contain an entry at prevLogIndex whose term matches prevLogTerm
            if (node.logEntryTerm(prevLogIndex) != -1 && node.logEntryTerm(prevLogIndex) != prevLogTerm) {
                LOG.info(node.getName() + ": log doesn��t contain an entry at prevLogIndex!");
                return new AppendEntryRes(currentTerm, false);
            }

            /** If an existing entry conflicts with a new one (same index
             * but different terms), delete the existing entry and all that
             * follow it
             */
            if (node.logEntryTerm(prevLogIndex + 1) != -1
                    && node.logEntryTerm(prevLogIndex + 1) != param.getEntry()[0].getEntryTerm()) {
                node.logDeleteFrom(prevLogIndex + 1);
            }

            // Append any new entries not already in the log
            for (Entry e : entries) {
                node.addEntry(e);
            }

            LOG.info(node.getName() + ": Now I will append the log!");

            if (leaderCommit > node.getCommitIndex()) {
                int i = Math.min(leaderCommit, node.lastLogIndex());
                node.setCommitIndex(i);
            }

            return new AppendEntryRes(currentTerm, true);
        } finally {
            appendLock.unlock();
        }
    }

    // a new peer will run this method on one of the existing peer
    // and get itself added to the list
    @Override
    public String addPeer(String address) throws RemoteException {
        // TODO Auto-generated method stub
        if (node.isLeader() && node.getPeer(address) == null) {
            String[] addr = address.split(":");
            node.addPeer(address, new InetSocketAddress(addr[0], Integer.parseInt(addr[1])));
            LOG.info(node.getName() + ": Now I will add a new peer!");
            return "done";
        }
        return node.getLeader();
    }

}
