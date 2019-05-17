package interfaces;

import java.rmi.RemoteException;

/**
  * @author Kuan Tian
  * 2019-05-06
  */

/* each server is treated as a node
 * implement the operators for 
 * requestvote RPC and appendEntry RPC */

public interface NodeInterf {
	public void launch() throws RemoteException; // start the server
	
	public String executeStateMachine(String command); // run the stateMachine
	
	public String handleRequest(String command); // handle the requests from the client
}