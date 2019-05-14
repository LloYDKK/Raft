package game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import implement.Node;


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
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Socket client = server.accept();
						new ServerThread(client).execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	class ServerThread implements Runnable {
		private Socket client;

		public ServerThread(Socket client) {
			this.client = client;
		}

		public void execute() {
			try {
				PrintStream outPut = new PrintStream(client.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String request = br.readLine();
				String gameResult = node.handleRequest(request);
				outPut.println(gameResult);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			execute();
		}
	}

}

