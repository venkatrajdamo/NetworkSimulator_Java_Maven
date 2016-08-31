package com.feedzai.netsim.engine;

import java.util.LinkedList;

/**
 * Represents the path followed by a packet in the network.
 */
public class NetworkPath {

	LinkedList<String> path;
	int time;
	
	/**
	 * Default Constructor
	 */
	public NetworkPath(){
		this.path = new LinkedList<String>();
		this.time = 0;
	}
	
	/**
	 * Constructor to initialize with Shortest path and time 
	 * @param foundPath Shortest Path
	 * @param foundTime Short Latency
	 */
	public NetworkPath(LinkedList<String> foundPath,int foundTime){
		this.path = foundPath;
		this.time = foundTime;
	}
	
	/**
	 * Overloaded method to return the path as string
	 */
	public String toString() {
		return path.toString().replace(" ", "");
    }
	
	
	/**
     * Time token to follow this path.
     * @return The time (ms)
     */
    public int getTime() {
        // TODO: Change this so that it corresponds to the time taken to follow the path
    	return this.time;
    }
}