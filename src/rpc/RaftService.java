package rpc;

/**
  * @author Kuan Tian
  * 2019-05-03
  */

public class RaftService implements RaftProtocol{
	public void hello() {
		System.out.println("Hello RPC");
	}
}
