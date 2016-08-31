package com.feedzai.netsim.example;


import com.feedzai.netsim.engine.*;

/**
 * Simple demonstration application for the network simulator.
 *
 * NOTE: You may need to add exception handling.
 */
public class Main {
	/**
	 * Main Method to Test the Network
	 * @param args Arguments
	 */
    public static void main(String[] args) {
        // Create a network with a default latency of 1 ms between nodes
        Network net = Network.createWithLatency(1);

        // Interconnect network elements
        net.connect("A", "D");                // Uses default network latency
        net.connect("B", "D");
        net.connect("C", "E");
        net.connect("I", "G");
        net.connect("J", "F");
        net.connect("K", "H", 10);            // Connect K computer to H router with a 10ms latency
        net.connect("D", "E", 3);             // D to E has a 3ms latency
        net.connect("D", "F", 2);             // D to F has a 2ms latency
        net.connect("E", "F", 4);             // E to F has a 4ms latency
        net.connect("E", "G", 5);             // E to G has a 5ms latency
        net.connect("G", "F", 3);             // G to F has a 3ms latency
        net.connect("F", "H", 5);             // F to H has a 5ms latency

     // Simulate sending a packet from "C" to "J"
        NetworkPath path = net.sendPacket("C", "J");

        // Print out the network path and how much time it took to send the packet
        System.out.println( path );
        System.out.println( path.getTime() );

     // Simulate sending a packet from "C" to "J" from cache
        path = net.sendPacket("C", "J");

        // Print out the network path and how much time it took to send the packet
        System.out.println( path );
        System.out.println( path.getTime() );
        
     // Simulate sending a packet from "J" to "C" from cache
        path = net.sendPacket("J", "C");

        // Print out the network path and how much time it took to send the packet
        System.out.println( path );
        System.out.println( path.getTime() );
        
    }
}