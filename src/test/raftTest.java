package test;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.Locale;

import entities.PeerList;
import raft.Node;

/**
  * @author Kuan Tian
  * 2019-05-10
  */

// this test can be run on one device
// need to change the address to local address
// localhost cannot be used here
public class raftTest {
	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "EN"));
		PeerList peerList = new PeerList();
        
		peerList.addPeer("10.12.16.244:8081", new InetSocketAddress("10.12.16.244",8081));
		peerList.addPeer("10.12.16.244:8082", new InetSocketAddress("10.12.16.244",8082));
		peerList.addPeer("10.12.16.244:8083", new InetSocketAddress("10.12.16.244",8083));
        peerList.addPeer("10.12.16.244:8084", new InetSocketAddress("10.12.16.244",8084));
		
		Node n1 = new Node(8081,peerList,8881);
		Node n2 = new Node(8082,peerList,8882);
		Node n3 = new Node(8083,peerList,8883);
        Node n4 = new Node(8084,peerList,8884);
		
		Thread t1 = new Thread() {
			public void run() {
				try {
					n1.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread t2 = new Thread() {
			public void run() {
				try {
					n2.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Thread t3 = new Thread() {
			public void run() {
				try {
					n3.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
        Thread t4 = new Thread() {
			public void run() {
				try {
					n4.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
        
		t1.start();
		t2.start();
		t3.start();
        t4.start();
	}
}
