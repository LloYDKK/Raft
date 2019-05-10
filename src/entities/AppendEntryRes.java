package entities;

import java.io.Serializable;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class AppendEntryRes implements Serializable{
	private int term;
	private boolean success;
	
	public AppendEntryRes(int t, boolean s) {
		term = t;
		success = s;
	}
	
	public AppendEntryRes(int t) {
		term = t;
	}
	
	public AppendEntryRes(boolean s) {
		success = s;
	}
	
	public int getTerm() {
		return term;
	}
	
}