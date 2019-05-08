package rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import implement.Consensus;

/**
  * @author Kuan Tian
  * 2019-05-03
  */

public class RPCServer{
	private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private HashMap<String,Class> serviceRegistry = new HashMap<String,Class>();
	private int port;
	
	public RPCServer(int port) {
		this.port = port;
	}
		
	public void start() throws IOException{
		ServerSocket server = new ServerSocket(port);
		try {
			while(true) {
				executor.execute(new ServerThread(server.accept()));
			}
		}
		finally {
			server.close();
		}
	}
	
	class ServerThread extends Thread{
		Socket client = null;
		
		public ServerThread(Socket sc) {
			client = sc;
		}
		
		public void run() {
			ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                input = new ObjectInputStream(client.getInputStream());
                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                Object[] arguments = (Object[]) input.readObject();
                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + " not found");
                }
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), arguments);
 

                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
			
		}
	}
	
	public void stop() {
		executor.shutdown();
	}
	
	public void register(Class service) {
		serviceRegistry.put(service.getName(), service);
	}
}
