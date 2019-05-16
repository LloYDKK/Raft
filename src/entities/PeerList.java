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
	
	public void setLeader(String leaderID) {
		this.leaderID = leaderID;
	}
	
	public void addPeer(String name, InetSocketAddress addr) {
		peerList.put(name, addr);
	}
	
	public InetSocketAddress getPeer(String name) {
		return peerList.get(name);
	}
	
	public int peerAmount() {
		return peerList.size();
	}
	
	public ArrayList<InetSocketAddress> allPeers(String name) {
		ArrayList<InetSocketAddress> list = new ArrayList<InetSocketAddress>();
		for (String key : peerList.keySet()) {
			if(!key.equals(name)) list.add(peerList.get(key));
		}
		return list;
	}
	
	public String getLeader() {
		return leaderID;
	}
}
