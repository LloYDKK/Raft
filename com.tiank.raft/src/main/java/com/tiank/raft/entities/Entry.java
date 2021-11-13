package com.tiank.raft.entities;

import java.io.Serializable;

/**
 * @author Kuan Tian
 * 2019-05-07
 */
// store the entries as a tuple structure
// each entry contains a term and command
public class Entry implements Serializable {
    private int term;
    private String command;

    public Entry() {
    }

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
