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
		
		public GameServer(Node n,int port) {
			node = n;
			this.port = port;
		}
		
	    public void launch() throws IOException
	    {
	    	ServerSocket server = new ServerSocket(port);
	    	Socket client = server.accept();
	    	try
		    {
	    		new Thread(new Runnable() {
	    			public void run() {
	    				try {
	    			    	PrintStream outPut = new PrintStream(client.getOutputStream());
	    			        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    			        String request = br.readLine();
	    			        System.out.println(request);
	    			        String gameResult= node.handleRequest(request);
	    		            outPut.println(gameResult);
	    				}catch(IOException e) {
	    					e.printStackTrace();
	    				}
	    			}
	    		}).start();	

		    }
		    catch(Exception e)
	        {
	            e.printStackTrace();
	        }   
	        
	    }
}

