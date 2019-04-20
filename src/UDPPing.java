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
		Integer prevPort = null;
		DatagramSocket succ = null; //Ping sent on this socket
		InetAddress addr = null; //Address of this machine
		byte[] buf = (peer.getId().toString() + " request").getBytes(); //ping message in buffer
		byte[] timeoutBuf = (peer.getId().toString() + " getSuccessors").getBytes(); //ping message for timeouts in buffer
		Boolean timeoutFlag = false;
		
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
				DatagramPacket pingPacket = new DatagramPacket(buf, buf.length, addr, 50000 + destPort);
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
					if (System.currentTimeMillis() - peer.getTscSuc2() > 30000L) { //successor 2 has timed out. send alternate request
						timeoutFlag = true;
						prevPort = destPort;
					}
					destPort = peer.getSuccessor2();
					System.out.println("current time: " + System.currentTimeMillis() + " - tscSuc2: " + peer.getTscSuc2() + " = " + (System.currentTimeMillis() - peer.getTscSuc2()));
					
				} else {
					if (System.currentTimeMillis() - peer.getTscSuc1() > 30000L) { //successor 1 has timed out. send alternate request
						timeoutFlag = true;
						prevPort = destPort;
					}
					destPort = peer.getSuccessor1();
					System.out.println("current time: " + System.currentTimeMillis() + " - tscSuc1: " + peer.getTscSuc1() + " = " + (System.currentTimeMillis() - peer.getTscSuc1()));
				}
				
				if (timeoutFlag) {
					try {
						pingPacket = new DatagramPacket(timeoutBuf, timeoutBuf.length, addr, 50000 + prevPort);
						succ.send(pingPacket);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						timeoutFlag = false;
					}
				}
				//close the socket
				succ.close();
			}
		} catch (SocketException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
