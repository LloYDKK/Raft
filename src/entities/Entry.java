package entities;

/**
  * @author Kuan Tian
  * 2019-05-07
  */

// store the entries as a tuple structure
public class Entry{
	private int term;
	private String command;
	
	public Entry(int term, String command) {
		this.term = term;
		this.command = command;
	}
	
	public int getEntryTerm() {
		return term;
	}
	
	public String getCommand() {
		return command;
	}
}
