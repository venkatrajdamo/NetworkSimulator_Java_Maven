package com.feedzai.netsim.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the overall computer network.
 */
public class Network {
	
	//Thread-Safe Data Structure to hold network node and edges
	ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>> network;

	//Thread-Safe Data Structure to cache any previously found path
	ConcurrentHashMap<String,ConcurrentHashMap<String,NetworkPath>> pathCache;
	
	//to Store default Latency
	Integer defaultLatency;
	
	/**
	 * Static Method to initialize network and set default Latency
	 * @param latency Default Latency
	 * @return Network
	 */
    public static Network createWithLatency(int latency) {
    	//Initialization
    	Network newNetwork = new Network();
    	newNetwork.network = new ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>>();
    	newNetwork.pathCache = new ConcurrentHashMap<String,ConcurrentHashMap<String,NetworkPath>>();
		newNetwork.defaultLatency = latency;
        return newNetwork;
    }

    /**
	 * Add new connection to the Network with Default Latency
	 * @param idA Source Node
	 * @param idB Destination Node
	 */
    public void connect(String idA, String idB) {
    	this.connect(idA,idB,this.defaultLatency);
    }

    /**
     * Add new connection to the Network with Latency
	 * @param idA Source Node
	 * @param idB Destination Node
     * @param latency Latency
     */
    public void connect(String idA, String idB, int latency) {

    	try{
	    	if(this.network.containsKey(idA)){
	    		this.network.get(idA).put(idB, latency);
	    	}
	    	else{
	    		ConcurrentHashMap<String,Integer> nearestNodes = new ConcurrentHashMap<String,Integer>();
	    		nearestNodes.put(idB, latency);
	    		this.network.put(idA,nearestNodes);
	    	}
	
	    	if(this.network.containsKey(idB)){
	    		this.network.get(idB).put(idA, latency);
	    	}
	    	else{
	    		ConcurrentHashMap<String,Integer> nearestNodes = new ConcurrentHashMap<String,Integer>();
	    		nearestNodes.put(idA, latency);
	    		this.network.put(idB,nearestNodes);
	    	}
	
	    	//Clear cache when network changes
	    	this.pathCache = new ConcurrentHashMap<String,ConcurrentHashMap<String,NetworkPath>>();
    	}
    	catch(Exception e){//errors can be logged
    		System.out.print("Error: Cannot Establish Connection");
    	}
    }

    /**
     * Find the shortest path between nodes to send packet and cache the path
     * @param idA Source Node
     * @param idB Destination Node
     * @return NetworkPath
     */
    public NetworkPath sendPacket(String idA, String idB) {
    	HashSet<String> unVisited;
    	HashMap<String,String> predecessor;
    	HashMap<String,Integer> distanceFromSource;
    	LinkedList<String> path = null;
    	LinkedList<String> pathReverse;
    	
    	//Check cache for path
    	if(this.isAvailableInCache(idA, idB)){
    		return this.pathCache.get(idA).get(idB);	
    	}

    	unVisited = new HashSet<String>();
    	predecessor = new HashMap<String,String>();
    	distanceFromSource = new HashMap<String,Integer>();
    	predecessor.put(idA, "");
    	distanceFromSource.put(idA, 0);
    	unVisited.add(idA);
    	//Find shortest path
    	try{
	    	while(unVisited.size() > 0)
	    	{
	    		String node = findMinimumUnvisitedNode(unVisited,distanceFromSource);
	    		unVisited.remove(node);
	
	    		ConcurrentHashMap<String,Integer> nearestNode = this.network.get(node);
	
	    		for (Entry<String,Integer> target : nearestNode.entrySet())
	    		{
	    			int targetDist = distanceFromSource.get(target.getKey()) != null?distanceFromSource.get(target.getKey()):Integer.MAX_VALUE ;
	    			int sourceDist =  distanceFromSource.get(node) != null?distanceFromSource.get(node):Integer.MAX_VALUE ;
	    			if (targetDist > sourceDist + target.getValue())
	    			{
	    				distanceFromSource.put(target.getKey(), sourceDist +  target.getValue());
	    				predecessor.put(target.getKey(), node);
	    				unVisited.add(target.getKey());
	    			}
	    		}
	    	}
	
	    	//Extract path and reversed path 
	    	path = new LinkedList<String>();
	    	pathReverse = new LinkedList<String>();
	    	String target = idB;
	    	while(distanceFromSource.get(target)!=null)
	    	{
	    		path.addFirst(target);
	    		pathReverse.add(target);
	    		target = predecessor.get(target);
	    	}
	    	//Store the path in cache
	    	this.storeInCache(idA, idB, path, pathReverse, distanceFromSource.get(idB));
	
    	}
    	catch(Exception e){//errors can be logged
    		System.out.print("Error: Cannot Establish Connection");
    	}
    	return (new NetworkPath(path, distanceFromSource.get(idB)));
    }
    
    /**
     * Find the unvisited node with minimum latency
     * @param unVisited Unvisited Nodes
     * @param dist Distance from Source
     * @return minNode
     */
    public String findMinimumUnvisitedNode(HashSet<String> unVisited,HashMap<String,Integer> dist){
    	String minNode = null;
    	Integer minDist = Integer.MAX_VALUE;
    	for(String node : unVisited)
    	{
    		if(minNode == null)
    		{
    			minNode = node;
    			minDist = dist.get(minNode) != null ? dist.get(minNode):Integer.MAX_VALUE;
    		}
    		else 
    		{
    			Integer distance = dist.get(node) != null ? dist.get(node): Integer.MAX_VALUE; 
    			if(distance<minDist)
    			{
    				minNode = node;
    				minDist = distance;
    			}
    		}
    	}
    	return minNode;
    }
    
    /***
     * Find the path to be found is already in cache
     * @param idA Source Node
     * @param idB Destination Node
     * @return Boolean
     */
    public Boolean isAvailableInCache(String idA, String idB){
    	if(this.pathCache.get(idA) != null){
    		if(this.pathCache.get(idA).get(idB) != null){
    			System.out.println("In Cache");
    			return true;
    		}
    	}
    	return false;
    }
    
    /***
     * Store the found path and reversed in path in cache
     * @param idA Source Node
     * @param idB Destination Node
     * @param path Path
     * @param pathReverse Reversed Path
     * @param time Time
     */
    public void storeInCache(String idA, String idB, LinkedList<String> path, LinkedList<String> pathReverse, Integer time){
    	NetworkPath shortestPath = new NetworkPath(path, time);
    	NetworkPath shortestPathReverse = new NetworkPath(pathReverse, time);
    	
    	if(this.pathCache.get(idA) != null)
    	{
    		this.pathCache.get(idA).put(idB, shortestPath);
    	}
    	else
    	{
    		ConcurrentHashMap<String,NetworkPath> pathToDestinations = new ConcurrentHashMap<String,NetworkPath>();
    		pathToDestinations.put(idB, shortestPath);
    		this.pathCache.put(idA,pathToDestinations);
    	}
    	
    	
    	if(this.pathCache.get(idB) != null)
    	{
    		this.pathCache.get(idB).put(idA, shortestPathReverse);
    	}
    	else
    	{
    		ConcurrentHashMap<String,NetworkPath> pathToDestinations = new ConcurrentHashMap<String,NetworkPath>();
    		pathToDestinations.put(idA, shortestPathReverse);
    		this.pathCache.put(idB,pathToDestinations);
    	}
    	
    }
}