package com.tiank.raft.interfaces;

import java.rmi.RemoteException;

/**
 * @author Kuan Tian
 * 2019-05-06
 */
/* each server is treated as a node
 * implement the operators for
 * requestVote RPC and appendEntry RPC */
public interface NodeInterface {
    void launch() throws RemoteException; // start the server

    String executeStateMachine(String command); // run the stateMachine

    String handleRequest(String command); // handle the requests from the client
}