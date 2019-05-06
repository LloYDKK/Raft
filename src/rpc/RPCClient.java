package rpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class RPCClient<T> {
    @SuppressWarnings("unchecked")
	public static <T> T getRemoteProxyObj(final Class<?> service, final InetSocketAddress addr) {

        return (T) Proxy.newProxyInstance(service.getClassLoader(), service.getInterfaces(),
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = null;
                        ObjectOutputStream output = null;
                        ObjectInputStream input = null;
                        try {

                            socket = new Socket();
                            socket.connect(addr);
 
                            output = new ObjectOutputStream(socket.getOutputStream());
                            output.writeUTF(service.getName());
                            output.writeUTF(method.getName());
                            output.writeObject(method.getParameterTypes());
                            output.writeObject(args);
 

                            input = new ObjectInputStream(socket.getInputStream());
                            return input.readObject();
                        } finally {
                            if (socket != null) socket.close();
                            if (output != null) output.close();
                            if (input != null) input.close();
                        }
                    }
                });
    }
    
    public static void main(String[] args) {
    	RaftProtocol raft = RPCClient.getRemoteProxyObj(RaftService.class, new InetSocketAddress("localhost", 8081));
    	raft.hello();
    }
}
