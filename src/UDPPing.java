import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPPing implements Runnable {
	private Peer peer;
		
	UDPPing (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {
		System.out.println("UDPPing started");
		Integer destPort = peer.getSuccessor1(); //Initial successor peer
		DatagramSocket succ = null; //Ping sent on this socket
		InetAddress addr = null; //Address of this machine
		byte[] buf = (peer.getId().toString() + " request").getBytes(); //ping message in buffer
		Integer length = buf.length; //length of ping message
		
		//Get address of this machine
		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		//Pinging loop
		try {
			while (true) {
				System.out.println("Ping " + (50000 + destPort));
				//Get an unused socket and build the ping packet
				succ = new DatagramSocket(); 
				DatagramPacket pingPacket = new DatagramPacket(buf, length, addr, 50000 + destPort);
				
				//send the ping packet
				try {
					succ.send(pingPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//wait for X milliseconds
				Thread.sleep(5000);
				
				//get the port number of the other peer, alternating between successors
				if (destPort == peer.getSuccessor1()) {
					destPort = peer.getSuccessor2();	
				} else {
					destPort = peer.getSuccessor1();
				}
				
				//close the socket
				succ.close();
			}
		} catch (SocketException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
