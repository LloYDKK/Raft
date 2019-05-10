package test;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;

import entities.PeerList;
import implement.Node;

/**
  * @author Kuan Tian
  * 2019-05-10
  */

public class raftTest {
	public static void main(String[] args) {
		PeerList peerList = new PeerList();
		peerList.addPeer("192.168.1.2:8081", new InetSocketAddress("192.168.1.2",8081));
		peerList.addPeer("192.168.1.2:8082", new InetSocketAddress("192.168.1.2",8082));
		peerList.addPeer("192.168.1.2:8083", new InetSocketAddress("192.168.1.2",8083));
		
		Node n1 = new Node(8081,peerList);
		Node n2 = new Node(8082,peerList);
		Node n3 = new Node(8083,peerList);
		
		
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