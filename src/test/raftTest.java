package test;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;

import entities.PeerList;
import implement.Node;

/**
  * @author Kuan Tian
  * 2019-05-10
  */

// this test can be run on one device
// need to change the address to local address first
public class raftTest {
	public static void main(String[] args) {
		PeerList peerList = new PeerList();
		peerList.addPeer("10.12.13.33:8081", new InetSocketAddress("10.12.13.33",8081));
		peerList.addPeer("10.12.13.33:8082", new InetSocketAddress("10.12.13.33",8082));
		peerList.addPeer("10.12.13.33:8083", new InetSocketAddress("10.12.13.33",8083));
		
		Node n1 = new Node(8081,peerList,8881);
		Node n2 = new Node(8082,peerList,8882);
		Node n3 = new Node(8083,peerList,8883);
		
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
		
		Thread t2 = new Thread() {
			public void run() {
				try {
					n2.init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread t3 = new Thread() {
			public void run() {
				try {
					n3.init();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		t1.start();
		t2.start();
		t3.start();
	}
}
