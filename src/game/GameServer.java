package game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import raft.Node;


/**
  * @author Kuan Tian
  * 2019-05-11
  */

public class GameServer {
	private Node node;
	private int port;

	public GameServer(Node n, int port) {
		node = n;
		this.port = port;
	}

	public void launch() throws IOException {

		ServerSocket server = new ServerSocket(port);
		while (true) {
			try {
				Socket client = server.accept();
				new ServerThread(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ServerThread extends Thread {
		private Socket client;
		PrintStream outPut;
		BufferedReader br;

		public ServerThread(Socket c) {
			client = c;
			try {
				outPut = new PrintStream(client.getOutputStream());
				br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			start();

		}

		public void run() {
			while (true) {
				try {
					String request = br.readLine();
					String gameResult = node.handleRequest(request);
					outPut.println(gameResult);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}

