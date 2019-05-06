package entities;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class AppendEntryRes {
	private int term;
	private boolean success;
	
	public AppendEntryRes(int t, boolean s) {
		this.term = t;
		this.success = s;
	}
	
	public AppendEntryRes(int t) {
		this.term = t;
	}
	
	public AppendEntryRes(boolean s) {
		this.success = s;
	}
	
}