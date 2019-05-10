package raft;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

/* each server is treated as a node
 * implement the operators for 
 * requestvote RPC and appendEntry RPC */

public interface NodeInterf {		
	public String redirectToLeader(String clientMessage); // redircet the client request to the leader
}
