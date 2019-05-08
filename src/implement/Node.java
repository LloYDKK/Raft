package implement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import entities.AppendEntryPar;
import entities.AppendEntryRes;
import entities.Entry;
import entities.Log;
import entities.PeerList;
import entities.RequestVotePar;
import entities.RequestVoteRes;
import entities.Status;
import raft.ConsensusInterf;
import raft.NodeInterf;
import rpc.RPCClient;
import rpc.RPCServer;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Node implements NodeInterf {
	
	private int status = Status.FOLLOWER;
	private String leaderID;
	private boolean receiveFromLeader;
	private static Logger LOG = Logger.getAnonymousLogger();
	private String name;
	private PeerList peerList;
	private ConsensusInterf raft;
	ExecutorService electExecutor;
	ExecutorService hbExecutor;
	ExecutorService RPCExecutor;
		
	/** Persistent state on all servers
	  * Updated on stable storage before 
	  * responding to RPCs
	  */
	private int currentTerm = 0; // latest term server has seen
	private String votedFor; // candidateId that received vote in current term 
	private Log log; //// log entries
	
	// Volatile state on all servers
	private int commitIndex; // index of highest log entry known to be committed 
	int lastApplied; // index of highest log entry applied to state machine 
	
	// Volatile state on leaders
	HashMap<Node,Integer> nextIndex; // for each server, index of the next log entry to send to that server
	HashMap<Node,Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server
	
	public Node() {
		log = new Log();
	}
	
	// getter and setter
	public void setStatus(int s) {
		status = s;
	}
	
	public void setLeader(String l) {
		leaderID = l;
	}
	
	public int getCurrentTerm() {
		return currentTerm;
	}
	
	public void setCurrentTerm(int ct) {
		currentTerm = ct;
	}
	
	public String getVotedFor() {
		return votedFor;
	}
	
	public void setVotedFor(String vf) {
		votedFor = vf;
	}
	
	public int getCommitIndex() {
		return commitIndex;
	}
	
	public void setCommitIndex(int ci) {
		commitIndex = ci;
	}
	
	public void receiveFromLeader() {
		receiveFromLeader = true;
	}
	
	
	/* log opeator */
	public int lastLogIndex() {
		return log.getLastLogIndex();
	}
	
	public int lastLogTerm() {
		return log.getLastLogTerm();
	}
	
	public int logEntryTerm(int index) {
		return log.getEntryTerm(index);
	}
	
	public String logEntryCommand(int index) {
		return log.getCommand(index);
	}
	
	public void logDeleteFrom(int index) {
		log.deleteFrom(index);
	}
	
	public void addEntry(Entry entry) {
		log.append(entry);
	}
	
	// init the node
	public void init() {
		boolean running = true;
		new Consensus(this);
		electExecutor = Executors.newFixedThreadPool(1);
		hbExecutor = Executors.newFixedThreadPool(1);
		RPCExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// start the RPC service
		new Thread(new Runnable() {
			public void run() {
				try {
					RPCServer server = new RPCServer(8088);
					server.register(Consensus.class);
					server.start();
					
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		}).start();	
		
		while(running) {
			electExecutor.submit(new Elect());
			hbExecutor.submit(new HeartBeat());
		}
	}
	
	// election operation
	class Elect implements Runnable{

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(status == Status.LEADER) return; // if self status is leader, don't need to elect
			
			receiveFromLeader = false;
			
			// waiting for messages from the leader
			long electionTimeOut = (long) (Math.random()*150 + 150) ;
			
			try {
				Thread.sleep(electionTimeOut);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(receiveFromLeader) return;
			
			/** On conversion to candidate, start election: 
			  * Increment currentTerm 
			  * Vote for self 
			  * Reset election timer 
			  * Send RequestVote RPCs to all other servers
			  */
			status = Status.CANDIDATE;
			LOG.info("no message from leader, start election!");
			
			currentTerm += 1;
			votedFor = name;
			electionTimeOut = (long) (Math.random()*150 + 150) ;
			
			ArrayList<InetSocketAddress> peers = peerList.allPeers(name);
			ArrayList<Future> futureList = new ArrayList<Future>();
			
			RequestVotePar.Builder builder = new RequestVotePar.Builder();
			
			
			// send RPC to others
			// future list is needed
			for(InetSocketAddress peer : peers) {
				futureList.add(RPCExecutor.submit(new Callable() {

					@Override
					public Object call() throws Exception {
						// TODO Auto-generated method stub
						int lastLT = 0;
						int lastLI = 0;
						if(lastLogIndex() != -1) {
							lastLT = lastLogTerm();
							lastLI = lastLogIndex();
						}
						
						RequestVotePar param = builder.term(currentTerm)
													  .candidateId(name)
													  .lastLogIndex(lastLI)
													  .lastLogTerm(lastLT).build();
						
						raft = RPCClient.getRemoteProxyObj(Consensus.class, peer);
						RequestVoteRes response = raft.requestVote(param);
						return response;
						}
					}));
				}
			
			AtomicInteger success = new AtomicInteger(0); // store the number of granted replies
            CountDownLatch latch = new CountDownLatch(futureList.size());
            
            // waiting for the replies
            for(Future future : futureList) {
            	RPCExecutor.submit(new Callable() {

					@Override
					public Object call() throws Exception {
						// TODO Auto-generated method stub
						try{
							RequestVoteRes response = (RequestVoteRes) future.get(3,TimeUnit.SECONDS);
							if(response==null) return -1;
						
							boolean granted = response.getGranted();
						
							// increment the number of granted node
							if(granted) success.incrementAndGet();
							
							// update the current term
							else { 
								int term = response.getTerm();
								if(term >= getCurrentTerm()) setCurrentTerm(term);
								}
							return 0;
							}
						catch(Exception e) {
							return -1;
						}
						finally{
							latch.countDown();
							}
					}
            	});
            }
			
            try {
				latch.await(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // during the waiting, if receive heartbeat, then abort election
            if(status == Status.FOLLOWER) return;
            
            // the voting state
            int granted = success.get();
            
            LOG.info("the number of granted number is: "+granted);
            
            // become the leader if vote > 2
            if(granted >= peerList.peerAmount() / 2) {
            	LOG.info("now "+name+" is the leader");
            	status = Status.LEADER;
            	peerList.setLeader(name);
            }
            
            votedFor = "";
            
			}
		}
	
	// heartbeat operation
	class HeartBeat implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(status!=Status.LEADER) return; // if self status is not leader, dont need to send heartbeat
		}}
	
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
