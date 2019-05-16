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
	public void init() throws RemoteException; // init the node
}