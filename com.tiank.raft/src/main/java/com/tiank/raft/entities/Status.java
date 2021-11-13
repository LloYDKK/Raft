package com.tiank.raft.entities;

/**
 * @author Kuan Tian
 * 2019-05-06
 */
/* the status of the nodes */
public class Status {
    public static final int FOLLOWER = 0;
    public static final int CANDIDATE = 1;
    public static final int LEADER = 2;
}
