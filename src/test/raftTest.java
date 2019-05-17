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
// need to change the address to local address first
public class raftTest {
	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "EN"));
		PeerList peerList = new PeerList();
		//PeerList peerList2 = new PeerList();
		peerList.addPeer("10.12.22.176:8081", new InetSocketAddress("10.12.22.176",8081));
		//peerList.addPeer("10.12.22.176:8082", new InetSocketAddress("10.12.22.176",8082));
		//peerList.addPeer("10.12.230.17:8083", new InetSocketAddress("10.12.230.17",8083));
		//peerList.addPeer("10.12.22.176:8083", new InetSocketAddress("10.12.22.176",8083));
		//peerList2.addPeer("10.12.22.176:8081", new InetSocketAddress("10.12.22.176",8081));
		//peerList2.addPeer("10.12.22.176:8082", new InetSocketAddress("10.12.22.176",8082));
		
		Node n1 = new Node(8081,peerList,8881);
		//Node n2 = new Node(8082,peerList2,8882);
		//Node n3 = new Node(8083,peerList,8883);
		
		Thread t1 = new Thread() {
			public void run() {
				try {
					n1.init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
//		Thread t2 = new Thread() {
//			public void run() {
//				try {
//					n2.init();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
		
//		Thread t3 = new Thread() {
//			public void run() {
//				try {
//					n3.init();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
		
//		Thread t4 = new Thread() {
//			public void run() {
//				try {
//					int username = 100;
//					while(username<200) {
//					Thread.sleep(10000);
//					System.out.println(n1.handleRequest("register|"+username+"|222|333"));
//					//System.out.println(n2.handleRequest("register|"+username+"|222|333"));
//					//System.out.println(n3.handleRequest("register|"+username+"|222|333"));
//					username += 1;
//					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		};
		
		t1.start();
		//t2.start();
		//t3.start();
		//t4.start();
	}
}
