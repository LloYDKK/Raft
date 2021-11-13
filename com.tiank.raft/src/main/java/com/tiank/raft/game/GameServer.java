package com.tiank.raft.game;

import com.tiank.raft.raft.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Kuan Tian
 * 2019-05-11
 */
public class GameServer {
    private final Node node;
    private final int port;

    // implement a server using socket
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

    // implement multiple thread server to support multi-clients
    class ServerThread extends Thread {
        private final Socket client;
        PrintStream outPut;
        BufferedReader br;

        public ServerThread(Socket c) {
            client = c;
            start();

        }

        public void run() {
            try {
                outPut = new PrintStream(client.getOutputStream());
                br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String request = br.readLine();
                String gameResult = node.handleRequest(request);
                outPut.println(gameResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


