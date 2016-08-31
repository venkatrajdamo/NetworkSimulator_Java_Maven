package com.feedzai.netsim.engine;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;

/**
 * Tests Network class.
 */
public class NetworkTest {

	Network net;
	
	/***
	 * Initializing the network before each test
	 */
	@Before
	public void before(){
		// Create a network with a default latency of 1 ms between nodes
        net = Network.createWithLatency(1);

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
	}
	
	/***
	 * Test Send Packet Over Network
	 * @throws Exception Throwing exception
	 */
	@Test
    public void testSendPacket() throws Exception {
        

        // Simulate sending a packet from "C" to "J"
        NetworkPath path = net.sendPacket("C", "J");

        // Check followed path
        String followedPath = path.toString();
        String expectedPath = "[C,E,F,J]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        int takenTime = path.getTime();
        int expectedTime = 6;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);
    }
	
	/***
	 * Test the path caching
	 * @throws Exception Throwing exception
	 */
	@Test
    public void testSendPacketCache() throws Exception {
        
		assertEquals("Path should not be cached before sending packet.", false, net.isAvailableInCache("C","J"));
		
        // Simulate sending a packet from "C" to "J"
        NetworkPath path = net.sendPacket("C", "J");

        // Check followed path
        String followedPath = path.toString();
        String expectedPath = "[C,E,F,J]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        int takenTime = path.getTime();
        int expectedTime = 6;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);
        
        assertEquals("Path Not Cached.", true, net.isAvailableInCache("C","J"));
    }
	
	/***
	 * Test reversed path is caching
	 * @throws Exception Throwing exception
	 */
	@Test
    public void testSendPacketReverseCache()  throws Exception {
        
		assertEquals("Path should not be cached before sending packet.", false, net.isAvailableInCache("I","K"));
		
		// Simulate sending a packet from "C" to "J"
        NetworkPath path = net.sendPacket("K", "I");

        // Check followed path
        String followedPath = path.toString();
        String expectedPath = "[K,H,F,G,I]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        int takenTime = path.getTime();
        int expectedTime = 19;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);
        
        assertEquals("Path should not be cached before sending packet.", true, net.isAvailableInCache("I","K"));
    }
	
	/***
	 * Test cache storing method
	 * @throws Exception Throwing exception
	 */
	@Test
    public void testCacheStoring() throws Exception {
        
		assertEquals("Path should not be cached before sending packet.", false, net.isAvailableInCache("I","K"));
		assertEquals("Path should not be cached before sending packet.", false, net.isAvailableInCache("K","I"));
		
		LinkedList<String> path = new LinkedList<String>();
		path.addFirst("K");
		path.addFirst("H");
		path.addFirst("F");
		path.addFirst("G");
		path.addFirst("I");
		
		LinkedList<String> pathReversed = new LinkedList<String>();
		pathReversed.add("K");
		pathReversed.add("H");
		pathReversed.add("F");
		pathReversed.add("G");
		pathReversed.add("I");
		
		net.storeInCache("K", "I", path, pathReversed, 19);
        
		assertEquals("Path should not be cached before sending packet.", true, net.isAvailableInCache("I","K"));
		assertEquals("Path should not be cached before sending packet.", true, net.isAvailableInCache("K","I"));
    }
	
	/***
	 * Test the connection establishing between nodes
	 * @throws Exception Throwing exception
	 */
	@Test
    public void testConnection() throws Exception{
        

        // Simulate sending a packet from "C" to "J"
		int size = net.network.size();
        net.connect("Z", "J");
        assertNotEquals("Connection not created.", size, net.network.size());
    }
	

	/***
	 * Test sending packet in thread pool
	 * @throws Exception Throwing exception
	 */
	@Test(timeout=2000)
    public void testSendPacketInThreadPool() throws Exception {
                
        ExecutorService service = Executors.newFixedThreadPool(2);
        
        Future<NetworkPath> p1 = service.submit(new Callable<NetworkPath>(){

			@Override
			public NetworkPath call() {
				NetworkPath path = net.sendPacket("A", "I");
		        return path;
			}
        });
        Future<NetworkPath> p2 = service.submit(new Callable<NetworkPath>(){

			@Override
			public NetworkPath call() {
				NetworkPath path = net.sendPacket("B", "J");
				return path;
			}
        });
        Future<NetworkPath> p3 = service.submit(new Callable<NetworkPath>(){

			@Override
			public NetworkPath call() {
				NetworkPath path = net.sendPacket("K", "I");
				return path;
			}
        });
        

        service.shutdown();
        try {
        	service.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
        }

        String followedPath = p1.get().path.toString();
        String expectedPath = "[A, D, F, G, I]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        int takenTime = p1.get().getTime();
        int expectedTime = 7;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);     
        
        followedPath = p2.get().path.toString();
        expectedPath = "[B, D, F, J]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        takenTime = p2.get().getTime();
        expectedTime = 4;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);     
        
        followedPath = p3.get().path.toString();
        expectedPath = "[K, H, F, G, I]";
        assertEquals("Packet didn't follow expected path.", expectedPath, followedPath);

        // Check taken time
        takenTime = p3.get().getTime();
        expectedTime = 19;
        assertEquals("Packet didn't take the expected time.", expectedTime, takenTime);     
                        
	}
}