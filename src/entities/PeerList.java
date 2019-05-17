package entities;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
  * @author Kuan Tian
  * 2019-05-08
  */

// store all the peers and their addresses
public class PeerList implements Serializable{
	private Map<String,InetSocketAddress> peerList;
	private String leaderID = "";
	
	public PeerList() {
		peerList = new HashMap<String,InetSocketAddress>();
	}
	
	// update the leader's address
	public void setLeader(String leaderID) {
		this.leaderID = leaderID;
	}
	
	// add a new peer to the list
	public void addPeer(String name, InetSocketAddress addr) {
		peerList.put(name, addr);
	}
	
	// get the peer's address
	public InetSocketAddress getPeer(String name) {
		return peerList.get(name);
	}
	
	// return the amount of the peers
	public int peerAmount() {
		return peerList.size();
	}
	
	// given a peer, return all the peers that aside from itself
	public ArrayList<InetSocketAddress> allPeers(String name) {
		ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		for (String key : peerList.keySet()) {
			if(!key.equals(name)) list.add(peerList.get(key));
		}
		return list;
	}
	
	// return the leader's address
	public String getLeader() {
		return leaderID;
	}
	
	// remove a peer from the list
	public void removePeer(String name) {
		peerList.remove(name);
	}
}
