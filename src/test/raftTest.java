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
		PeerList peerList2 = new PeerList();
		PeerList peerList3 = new PeerList();
		
		// node1 only knows the address of itself
		peerList.addPeer("10.12.22.176:8081", new InetSocketAddress("10.12.22.176",8081));

		// node2 and node3 know the addresses of itself and of node1
		// after launching, peerLists on three node will be unified
		// node2 & node3 will appear as new members
		peerList2.addPeer("10.12.22.176:8081", new InetSocketAddress("10.12.22.176",8081));
		peerList2.addPeer("10.12.22.176:8082", new InetSocketAddress("10.12.22.176",8082));

		peerList3.addPeer("10.12.22.176:8081", new InetSocketAddress("10.12.22.176",8081));
		peerList3.addPeer("10.12.22.176:8083", new InetSocketAddress("10.12.22.176",8083));
		
		Node n1 = new Node(8081,peerList,8881);
		Node n2 = new Node(8082,peerList2,8882);
		Node n3 = new Node(8083,peerList3,8883);
		
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
					Thread.sleep(2000);
					n2.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread t3 = new Thread() {
			public void run() {
				try {
					Thread.sleep(3000);
					n3.launch();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
//        // t4 is used to test log replication between three nodes
//		Thread t4 = new Thread() {
//			public void run() {
//				try {
//					int username = 100;
//					while(username<200) {
//					Thread.sleep(10000);
//					System.out.println(n1.handleRequest("register|"+username+"|222|333"));
//					System.out.println(n2.handleRequest("register|"+username+"|222|333"));
//					System.out.println(n3.handleRequest("register|"+username+"|222|333"));
//					username += 1;
//					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
		
		t1.start();
		t2.start();
		t3.start();
		//t4.start();
	}
}
