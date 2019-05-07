package entities;

import java.util.HashMap;
import java.util.Map;

/**
  * @author Kuan Tian
  * 2019-05-04
  */

public class Log {
	private int index = 1;
	private Map<Integer,Integer> termEntries;
	private Map<Integer,String> commandEntries;
	
	public Log() {
		termEntries = new HashMap<Integer,Integer>();
		commandEntries = new HashMap<Integer,String>();
	}
	
	// append a new entry containing term and command
	public void append(int term,String command) {
		index += 1;
		termEntries.put(index,term);
		commandEntries.put(index, command);
	}
	
	// get the last index in the log
	public int getLastLogIndex() {
		return index;
	}
	
	// get the term of the last entry in the log
	public int getLastLogTerm() {
		return termEntries.get(index);
	}
	
	// get the last command
	public String getLastCommand() {
		return commandEntries.get(index);
	}
	
	// get term of the entry given the index
	public int getEntryTerm(int i) {
		return termEntries.get(i);
	}
	
	// get the commadn give the index
	public String getCommand(int i ) {
		return commandEntries.get(i);
	}
	
}
