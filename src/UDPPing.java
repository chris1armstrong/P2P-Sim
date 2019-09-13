//Written by Chris Armstrong, April 2019

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPPing implements Runnable {
	private Peer peer;
		
	UDPPing (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {
		Integer destPort = peer.getSuccessor1(); //Initial successor peer
		DatagramSocket succ = null; //Ping sent on this socket
		InetAddress addr = null; //Address of this machine
		byte[] buf; //ping message in buffer
		Integer seq = 0;
		Integer alternate = 0;
		
		//Get address of this machine
		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		//Pinging loop
		try {
			while (true) {
				if (peer.getRunning() == false) { //check if kill flag is set
					break;
				}
				//Get an unused socket and build the ping packet
				succ = new DatagramSocket();
				buf = (peer.getId().toString() + " request " + seq).getBytes();
				DatagramPacket pingPacket = new DatagramPacket(buf, buf.length, addr, 50000 + destPort);
				//send the ping packet
				try {
					succ.send(pingPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//wait for 5000 milliseconds
				Thread.sleep(5000);
				
				//get the port number of the other peer, alternating between successors
				if (alternate == 0) {
					if (peer.getSequenceNum2() - peer.getTscSuc2() > 2) { //successor 2 has timed out
						System.out.println("Peer " + peer.getSuccessor2() + " is no longer alive");
						peerTimeout(destPort, peer.getSequenceNum2());
						peer.setSequenceNum2(0);
					}
					peer.incrementSequenceNum2();
					seq = peer.getSequenceNum2();
					alternate = 1;
					destPort = peer.getSuccessor2();
				} else if (alternate == 1){
					if (peer.getSequenceNum1() - peer.getTscSuc1() > 2) { //successor 1 has timed out
						System.out.println("Peer " + peer.getSuccessor1() + " is no longer alive");
						peerTimeout(destPort, peer.getSequenceNum1());
						peer.setSequenceNum1(0);
						peer.setSequenceNum2(0);
					}
					peer.incrementSequenceNum1();
					seq = peer.getSequenceNum1();
					alternate = 0;
					destPort = peer.getSuccessor1();
				}
				//close the socket
				succ.close();
			}
		} catch (SocketException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private void peerTimeout(Integer destPort, Integer seq) { //Sends message to successor requesting its neighbours
		Socket nextPeer;
		try { //request your peer's successors, so this peer can update its successors
			nextPeer = new Socket("localhost",50000 + destPort);
			DataOutputStream outToPeer = new DataOutputStream(nextPeer.getOutputStream());
			DataInputStream inFromPeer = new DataInputStream(nextPeer.getInputStream());
			BufferedReader receiveRead = new BufferedReader(new InputStreamReader(inFromPeer));
			String request = new String(peer.getId() + " getSuccessors");
			outToPeer.writeBytes(request + '\n');
			// receive and parse the response message
			String received = receiveRead.readLine();
			nextPeer.close();
			String[] message = received.split("\\s+");
			if (destPort == peer.getSuccessor2()) {
				peer.setSuccessor1(peer.getSuccessor2());
				peer.setSuccessor2(Integer.parseInt(message[2]));
				peer.setTscSuc1(0);
				peer.setTscSuc2(0);
				System.out.println("My first successor is now peer " + peer.getSuccessor1());
				System.out.println("My second successor is now peer " + peer.getSuccessor2());
			} else if (destPort == peer.getSuccessor1()) {
				if (Integer.parseInt(message[2]) != peer.getSuccessor2()) {
					//successor has already updated its neighbour list, take its 1st successor
					peer.setSuccessor2(Integer.parseInt(message[2])); 
				} else {//successor has not updated its neighbour yet, take its 2nd successor
					peer.setSuccessor2(Integer.parseInt(message[3])); 
				}
				peer.setTscSuc2(0);

				System.out.println("My first successor is now peer " + peer.getSuccessor1());
				System.out.println("My second successor is now peer " + peer.getSuccessor2());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
