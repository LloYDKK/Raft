package implement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

/**
  * @author Kuan Tian
  * 2019-05-06
  */

public class Node implements NodeInterf {
	
	private static Logger LOG = Logger.getAnonymousLogger();
	
	private int status ;
	private boolean receiveFromLeader;
	private String name;
	private PeerList peerList;
	ExecutorService electExecutor;
	ExecutorService hbExecutor;
	ExecutorService RPCExecutor;
	private int port;
	private Consensus consensus;
		
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
	
	public Node(int port, PeerList peerList) {
		status = Status.FOLLOWER;
		this.port = port;
		this.peerList = peerList;
		log = new Log();

		try {
			consensus = new Consensus(this);
			name = InetAddress.getLocalHost().getHostAddress() + ":" +port;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// getter and setter
	public String getName() {
		return name;
	}
	
	public void setStatus(int s) {
		status = s;
	}
	
	public void setLeader(String l) {
		peerList.setLeader(l);
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
	public void init() throws RemoteException {
		boolean running = true;
		electExecutor = Executors.newFixedThreadPool(1);
		hbExecutor = Executors.newFixedThreadPool(1);
		RPCExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// start the RPC service
		new Thread(new Runnable() {
			public void run() {
				try {
					Registry registry = LocateRegistry.createRegistry(port);
					registry.bind("consensus", consensus);
				}catch(IOException e) {
					e.printStackTrace();
				} catch (AlreadyBoundException e) {
					// TODO Auto-generated catch block
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
			long electionTimeOut = (long) (Math.random()*1500 + 150) ;
			
			try {
				Thread.sleep(electionTimeOut);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(receiveFromLeader) {
				LOG.info(name + ": receiving message from leader!");
				return;
			}
			
			/** On conversion to candidate, start election: 
			  * Increment currentTerm 
			  * Vote for self 
			  * Reset election timer 
			  * Send RequestVote RPCs to all other servers
			  */
			status = Status.CANDIDATE;
			LOG.info(name + ": no message from leader, start election!");
			
			currentTerm += 1;
			votedFor = name;
			electionTimeOut = (long) (Math.random()*1500 + 150) ;
			
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
						int lastLT = lastLogTerm();
						int lastLI = lastLogIndex();
						
						RequestVotePar param = builder.term(currentTerm)
													  .candidateId(name)
													  .lastLogIndex(lastLI)
													  .lastLogTerm(lastLT).build();
						
						Registry registry = LocateRegistry.getRegistry(peer.getHostName(),peer.getPort());
						
						ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");
						
						RequestVoteRes response = consensus1.requestVote(param);
						LOG.info(name+"   "+response.getTerm()+"  "+response.getGranted());
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
            
            LOG.info(name+" the number of granted number is: "+granted);
            
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
			
			LOG.info(name + ": Sending heartbeat to all the peers");
			
			ArrayList<InetSocketAddress> peers = peerList.allPeers(name);
			
			AppendEntryPar.Builder builder = new AppendEntryPar.Builder();
			
			AppendEntryPar heartbeat = builder.leaderId(name)
                    .entries(new Entry())
                    .term(currentTerm)
                    .preLogIndex(0)
                    .preLogTerm(0)
                    .leaderCommit(0)
                    .build();
			
			for(InetSocketAddress peer : peers) {
				RPCExecutor.submit(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Registry registry = LocateRegistry.getRegistry(peer.getHostName(),peer.getPort());
							ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");
							AppendEntryRes response = consensus1.appendEntries(heartbeat);
							
							int term = response.getTerm();
							
							// convert to follower if term > currentTerm
							if(term>currentTerm) {
								LOG.info(name + "Now I become follower!");
								status = Status.FOLLOWER;
								currentTerm = term;
								votedFor = "";
							}
							
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NotBoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				
				
			}
			
			long heartBeatTime = 100 ;
			
			try {
				Thread.sleep(heartBeatTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}}
	
	
	@Override
	public String redirectToLeader(String clientMessage) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) {
		PeerList peerList = new PeerList();
		peerList.addPeer("192.168.1.2:8081", new InetSocketAddress("192.168.1.2",8081));
		peerList.addPeer("192.168.1.2:8082", new InetSocketAddress("192.168.1.2",8082));
		peerList.addPeer("192.168.1.2:8083", new InetSocketAddress("192.168.1.2",8083));
		
		Thread t1 = new Thread() {
			public void run() {
				try {
					new Node(8081,peerList).init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread t2 = new Thread() {
			public void run() {
				try {
					new Node(8082,peerList).init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread t3 = new Thread() {
			public void run() {
				try {
					new Node(8083,peerList).init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		
		t1.start();
		t2.start();
		t3.start();
	}
}
