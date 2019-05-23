package raft;

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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
import game.GameServer;
import interfaces.ConsensusInterf;
import interfaces.NodeInterf;

/**
 * @author Kuan Tian 
 * 2019-05-06
 */

// each server in the cluster appears as a node
// this class impelment the node structure
// also includes the impelmentation of heartbeat, election and log replication
public class Node implements NodeInterf {

	private static Logger LOG = Logger.getAnonymousLogger();

	private int status;  // the current status of the node
	private boolean receiveFromLeader;
	private String name; // stored as the address of the node
	private PeerList peerList;
	ExecutorService electExecutor; // the election executor
	ExecutorService hbExecutor; // the heartbeat executor
	ExecutorService RPCExecutor; // the RPC executor to call the functions in consensus class
	ExecutorService stateMachineExecutor; // the executor to run the statemachine
	ExecutorService ReplicationExecutor;  // the log replication executor
	ExecutorService countResultExecutor; // the thread used to count the successful result from other pear
	private int port; // the prot for the raft RPC
	private Consensus consensus; // a object to init the consensus algorithm on the node 
	private GameServer gameServer; // the game server

	/**
	 * Persistent state on all servers Updated on stable storage before responding
	 * to RPCs
	 */
	private int currentTerm = 0; // latest term server has seen
	private String votedFor; // candidateId that received vote in current term
	private Log log; //// log entries

	// Volatile state on all servers
	private int commitIndex; // index of highest log entry known to be committed
	int lastApplied; // index of highest log entry applied to state machine

	// Volatile state on leaders
	Map<InetSocketAddress, Integer> nextIndex; // for each server, index of the next log entry to send to that server
	Map<InetSocketAddress, Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server
	
	public Node(int port, PeerList peerList, int gamePort) {
		this.port = port;
		this.peerList = peerList;
		votedFor = "";
		commitIndex = 0;
		lastApplied = 0;
		status = Status.FOLLOWER;
		log = new Log();
		gameServer = new GameServer(this, gamePort);

		try {
			consensus = new Consensus(this);
			name = InetAddress.getLocalHost().getHostAddress() + ":" + port;
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

	public boolean isLeader() {
		return (status == Status.LEADER);
	}
	
	public int getLastApplied() {
		return lastApplied;
	}
	
	// the peerList operator
	public int getPeerAmount() {
		return peerList.peerAmount();
	}
	
	public InetSocketAddress getPeer(String name) {
		return peerList.getPeer(name);
	}

	public void addPeer(String name, InetSocketAddress addr) {
		peerList.addPeer(name, addr);
		nextIndex.put(addr, log.getLastLogIndex() + 1);
		matchIndex.put(addr, 0);
	}
	
	public String getLeader() {
		return peerList.getLeader();
	}
	
	public void setPeerList(PeerList peerList) {
		this.peerList = peerList;
	}
	
	public void setLeader(String l) {
		peerList.setLeader(l);
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

	// start the services on the node
	public void launch() throws RemoteException {
		boolean running = true;
		electExecutor = Executors.newFixedThreadPool(1);
		hbExecutor = Executors.newFixedThreadPool(1);
		RPCExecutor = Executors.newFixedThreadPool(4);
		ReplicationExecutor = Executors.newFixedThreadPool(1);
		stateMachineExecutor = Executors.newFixedThreadPool(1);
		countResultExecutor = Executors.newFixedThreadPool(1);

		// start the rmi server
		try {
			Registry registry = LocateRegistry.createRegistry(port);
			registry.bind("consensus", consensus);
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// start the game server
		new Thread(new Runnable() {
			public void run() {
				try {
					gameServer.launch();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		// join the peerList
		ArrayList<InetSocketAddress> peers = peerList.allPeers(name);
		
		RPCExecutor.submit(new Runnable() {
			InetSocketAddress peer = null;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String response = "";
					peer = peers.get(0);
					String[] addr = null;
					// if the peer is not the leader, retry adding self to the leader
					while (!response.equals("done")) {
						Registry registry = LocateRegistry.getRegistry(peer.getHostName(), peer.getPort());
						ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");
						response = consensus1.addPeer(name);
						if(!response.equals("done")) {
							addr = response.split(":");
						}
						peer = new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));
					}

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					peerList.removePeer(peer.getHostName()+":"+peer.getPort());
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		
		// execute the election and heartbeat
		while (running) {
		    try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			electExecutor.submit(new Elect());
			hbExecutor.submit(new HeartBeat());
		}
	}

	// implement election module as descirbed on the report
	class Elect implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (status == Status.LEADER) {
				return; // if self status is leader, don't need to elect
			}

			receiveFromLeader = false;
			LOG.info(name + ": the peer amount now is "+peerList.peerAmount());
			// waiting for messages from the leader
			long electionTimeOut = (long) (Math.random() * 5000 + 3000);

			try {
				Thread.sleep(electionTimeOut);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (receiveFromLeader) {
				return;
			}

			/**
			 * On conversion to candidate, start election: Increment currentTerm Vote for
			 * self Reset election timer Send RequestVote RPCs to all other servers
			 */
			status = Status.CANDIDATE;
			LOG.info(name + ": no message from leader, start election!");

			currentTerm += 1;
			votedFor = name;
			electionTimeOut = (long) (Math.random() * 5000 + 3000);

			ArrayList<InetSocketAddress> peers = peerList.allPeers(name);
			ArrayList<Future> futureList = new ArrayList<Future>(); // store the results received from other peers

			RequestVotePar.Builder builder = new RequestVotePar.Builder();

			// send RPC to others peers
			for (InetSocketAddress peer : peers) {
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

						
						Registry registry = LocateRegistry.getRegistry(peer.getHostName(), peer.getPort());

						ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");

						RequestVoteRes response = consensus1.requestVote(param);

						LOG.info(name + " Elect response from "+ peer.getPort() +": " + response.getTerm() + "  " + response.getGranted());
						return response;
					}
				}));
			}

			AtomicInteger success = new AtomicInteger(0); // store the number of granted replies
			CountDownLatch latch = new CountDownLatch(futureList.size());

			// waiting for the replies
			for (Future future : futureList) {
				countResultExecutor.submit(new Callable() {

					@Override
					public Object call() throws Exception {
						// TODO Auto-generated method stub
						try {
							RequestVoteRes response = (RequestVoteRes) future.get(10, TimeUnit.SECONDS);
							if (response == null) {
								return -1;
							}

							boolean granted = response.getGranted();

							// increment the number of granted node
							if (granted) {
								success.incrementAndGet();
							}

							// update the current term
							else {
								int term = response.getTerm();
								if (term >= getCurrentTerm()) {
									setCurrentTerm(term);
								}
							}
							return 0;
						} catch (Exception e) {
							return -1;
						} finally {
							latch.countDown();
						}
					}
				});
			}

			try {
				latch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// during the waiting, if receive heartbeat, abort election
			if (status == Status.FOLLOWER) {
				LOG.info(name + " New leader elected, abort election! ");
				return;
			}

			// the voting state
			int granted = success.get();

			LOG.info(name + " the number of granted number is: " + granted);

			// become the leader if vote > 2
			// reinitialize the nextIndex and matchIndex
			if (granted >= peerList.peerAmount() / 2) {
				LOG.info(name + ": now I am the leader");
				status = Status.LEADER;
				peerList.setLeader(name);
				nextIndex = new HashMap<InetSocketAddress, Integer>();
				matchIndex = new HashMap<InetSocketAddress, Integer>();
				for (InetSocketAddress peer : peers) {
					nextIndex.put(peer, log.getLastLogIndex() + 1);
					matchIndex.put(peer, 0);
				}
			}

			votedFor = "";

		}
	}

	// impelement heatbeat as descirbed on the report
	class HeartBeat implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (status != Status.LEADER)
				return; // if self status is not leader, dont need to send heartbeat

			ArrayList<InetSocketAddress> peers = peerList.allPeers(name);

			for (InetSocketAddress peer : peers) {
				RPCExecutor.submit(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							LOG.info(name + ": Sending heartbeat to " + peer.getPort());
							
							AppendEntryPar.Builder builder = new AppendEntryPar.Builder();

							AppendEntryPar heartbeat = builder.leaderId(name).entries(new Entry[0]).term(currentTerm)
									.preLogIndex(0).preLogTerm(0).leaderCommit(0).peerList(peerList).build();

							Registry registry = LocateRegistry.getRegistry(peer.getHostName(), peer.getPort());
							ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");
							AppendEntryRes response = consensus1.appendEntries(heartbeat);

							int term = response.getTerm();

							// convert to follower if term > currentTerm
							if (term > currentTerm) {
								LOG.info(name + ": Term is less than " + peer.getPort() + " Now I become follower!");
								status = Status.FOLLOWER;
								currentTerm = term;
								votedFor = "";
							}

						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							peerList.removePeer(peer.getHostName()+":"+peer.getPort());
						} catch (NotBoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}

			long heartBeatTime = 500;

			try {
				Thread.sleep(heartBeatTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	// execute the command on the state machine
	public String executeStateMachine(String command) {
		String response = "";
		Future f1 = stateMachineExecutor.submit(new StateMachine(command));
		try {
			response = f1.get().toString();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	// handle the request from the client
	public String handleRequest(String command) {
		LOG.info(name + " receive request from client: "+ command);
		if (status != Status.LEADER) {
			LOG.info(name + ": I am not the leader, redirect to the leader");
			if (peerList.getLeader().equals("")) {
				return "3#" + name;
			}
			return "3#" + peerList.getLeader();
		}
		
		/*  If command received from client: append entry to local log, 
		 *  respond after entry applied to state machine
		 */
		boolean result = false;
		log.append(new Entry(currentTerm,command));
		
		Future f = ReplicationExecutor.submit(new LogReplication(new Entry(currentTerm, command)));
		try {
			result = (boolean) f.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// execute the statemachine if successful
		if (result) {
			return "1#" + executeStateMachine(command);
		}
		return "2#";
	}

	// replicate the log to all the followers
	class LogReplication implements Callable {

		private Entry entry;

		public LogReplication(Entry entry) {
			this.entry = entry;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object call() throws Exception {
			// TODO Auto-generated method stub
			
			if(status!=Status.LEADER) {
				LOG.info(name+" I am not the leader, can't replicate");
				return false;
			}

			ArrayList<InetSocketAddress> peers = peerList.allPeers(name);
			ArrayList<Future> futureList = new ArrayList<Future>();
			AppendEntryPar.Builder builder = new AppendEntryPar.Builder();
			
			/* If last log index ¡Ý nextIndex for a follower: 
			 * send AppendEntries RPC with log entries starting at nextIndex 
			 * If successful: update nextIndex and matchIndex for follower
			 * If AppendEntries fails because of log inconsistency: decrement nextIndex and retry*/
			for (InetSocketAddress peer : peers) {
				futureList.add(RPCExecutor.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						// TODO Auto-generated method stub
						
						long current = System.currentTimeMillis();
						
						// retry in 10 seconds if fail
						while (System.currentTimeMillis() - current < 10000) {
							
							// replicate all the entries that are not stored on each peer
							int entryIndex = log.getLastLogIndex();
							int peerEntryIndex = nextIndex.get(peer);
							int preLogIndex = entryIndex;

							ArrayList<Entry> logToReplicated = new ArrayList<Entry>();

							if (entryIndex >= peerEntryIndex) {
								preLogIndex = peerEntryIndex;
								for (int i = peerEntryIndex; i <= entryIndex; i++) {
									Entry e = log.getEntry(i);
									if (e != null) {
										logToReplicated.add(e);
									}
								}
							} else{
								logToReplicated.add(log.getEntry(entryIndex));
							}
							
							AppendEntryPar param = builder.term(currentTerm)
														  .leaderId(name)
														  .preLogIndex(preLogIndex)
														  .preLogTerm(logToReplicated.get(0)
														  .getEntryTerm())
														  .entries(logToReplicated.toArray(new Entry[0]))
														  .leaderCommit(commitIndex)
														  .peerList(peerList).build();
							
							LOG.info(name+" :replicate log on "+peer.getPort());

							Registry registry = LocateRegistry.getRegistry(peer.getHostName(), peer.getPort());

							ConsensusInterf consensus1 = (ConsensusInterf) registry.lookup("consensus");
							
							AppendEntryRes response = consensus1.appendEntries(param);
							
							if (response == null) {
								LOG.info(name + " AppendLog Response: None");
								return false;
							}

							else {
								if (response.getSuccess()) {
									LOG.info(name +" to "+ peer.getHostName()+":"+peer.getPort() + " Log successfully appended");
									nextIndex.put(peer, entryIndex + 1);
									matchIndex.put(peer, entryIndex);
									return true;
								} else {
									if (response.getTerm() > currentTerm) {
										LOG.info(name + ": Term is less than "+peer.getPort()+" Now I become follower!");
										status = Status.FOLLOWER;
										return false;
									} else {
										nextIndex.put(peer, peerEntryIndex - 1);
										LOG.info(name +" to "+ peer.getHostName()+":"+peer.getPort() + " Restart replication due to error index.");
									}
								}
							}
						}
						return false;
					}
				}));
			}

			AtomicInteger success = new AtomicInteger(0); // store the number of granted replies
			CountDownLatch latch = new CountDownLatch(futureList.size());

			// waiting for the replies
			for (Future future : futureList) {
				countResultExecutor.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						// TODO Auto-generated method stub
						try {
							boolean response = (boolean) future.get(10, TimeUnit.SECONDS);

							// increment the number of granted node
							if (response) {
								success.incrementAndGet();
							}
							return 0;
						} catch (Exception e) {
							return -1;
						} finally {
							latch.countDown();
						}
					}
				});
			}

			try {
				latch.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// if converted to follower when replicating, abort
			if(status == Status.FOLLOWER) {
				LOG.info(name + " Now I become follower, abort changes.");
				return false;
			}
			
			// the appending state
			int appendSuccess = success.get();

			LOG.info(name + " the number of append success number is: " + appendSuccess);
			
			
			/* If there exists an N such that N > commitIndex, 
			 * a majority of matchIndex[i] ¡Ý N, 
			 * and log[N].term == currentTerm: 
			 * set commitIndex = N
			 */
			int majority = 0;
			for(InetSocketAddress peer : peers) { 
				if(matchIndex.get(peer)!= null) {
					majority += matchIndex.get(peer);
				}
			}
			if(matchIndex.size()>0) {
				majority /= matchIndex.size();
			}
			
			
			for(int N = commitIndex;N<=majority;N++) {
				if(log.getEntryTerm(N) == currentTerm) {
					commitIndex = N;
				}
			}
			
			// reinitialize the nextIndex and matchIndex
			if (appendSuccess >= peerList.peerAmount() / 2) {
				LOG.info(name + ": Most of the followers have appended the log, now I will commit");
				commitIndex = log.getLastLogIndex();
				lastApplied = commitIndex;
				return true;
			}
			LOG.info(name + ": Replication is not successful now I will not commit");
			return false;
		}
	}

}
