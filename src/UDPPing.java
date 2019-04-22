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
		System.out.println("UDPPing started");
		Integer destPort = peer.getSuccessor1(); //Initial successor peer
		//Integer prevPort = null;
		DatagramSocket succ = null; //Ping sent on this socket
		InetAddress addr = null; //Address of this machine
		byte[] buf; //ping message in buffer
		//byte[] timeoutBuf = (peer.getId().toString() + " getSuccessors").getBytes(); //ping message for timeouts in buffer
		Boolean timeoutFlag = false;
		Integer seq = 0;
		Integer seq1 = 0;
		Integer seq2 = 0;
		
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
				buf = (peer.getId().toString() + " request " + seq).getBytes();
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
					if (seq2 - peer.getTscSuc2() > 3) { //successor 2 has timed out. send alternate request
						timeoutFlag = true;
						//prevPort = destPort;
						//TCP request destPort for peers 
						peerTimeout(destPort, seq2);
						seq2 = 0;
					}
					seq2++;
					seq = seq2;
					destPort = peer.getSuccessor2();
					System.out.println("current seq: " + seq2 + " - tscSuc2: " + peer.getTscSuc2() + " = " + (seq2 - peer.getTscSuc2()));
				} else if (destPort == peer.getSuccessor2()){
					if (seq1 - peer.getTscSuc1() > 3) { //successor 1 has timed out. send alternate request
						timeoutFlag = true;
						//prevPort = destPort;
						peerTimeout(destPort,seq1);
						seq1 = 0;
						seq2 = 0;
					}
					seq1++;
					seq = seq1;
					destPort = peer.getSuccessor1();
					System.out.println("current seq: " + seq1 + " - tscSuc1: " + peer.getTscSuc1() + " = " + (seq1 - peer.getTscSuc1()));
				}
				
				if (timeoutFlag) { //Change to TCP request to prevPort
					System.out.println("timeout -> send TCP request");
					/*
					try {
						pingPacket = new DatagramPacket(timeoutBuf, timeoutBuf.length, addr, 50000 + prevPort);
						succ.send(pingPacket);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						timeoutFlag = false;
					}
					*/
					timeoutFlag = false;
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
		System.out.println("TCP sending peer getSuccessors request");
		try {
			nextPeer = new Socket("localhost",50000 + destPort);
			System.out.println("Socket Bound: " + (50000 + destPort));
			DataOutputStream outToPeer = new DataOutputStream(nextPeer.getOutputStream());
			DataInputStream inFromPeer = new DataInputStream(nextPeer.getInputStream());
			BufferedReader receiveRead = new BufferedReader(new InputStreamReader(inFromPeer));
			String request = new String(peer.getId() + " getSuccessors");
			outToPeer.writeBytes(request + '\n');
			System.out.println("Message sent");
			String received = receiveRead.readLine();
			System.out.println("Message received: " + received);
			nextPeer.close();
			
			String[] message = received.split("\\s+");
			if (destPort == peer.getSuccessor2()) {
				peer.setSuccessor1(peer.getSuccessor2());
				peer.setSuccessor2(Integer.parseInt(message[2]));
				peer.setTscSuc1(0);
				peer.setTscSuc2(0);
				System.out.println("Peer is successor 1. It has timed out. New succ1 = " + peer.getSuccessor1() + ". New succ2 = " + peer.getSuccessor2());
			} else if (destPort == peer.getSuccessor1()) {
				if (Integer.parseInt(message[2]) != peer.getSuccessor2()) { 
					peer.setSuccessor2(Integer.parseInt(message[2])); //successor has already updated its neighbour list, take its 1st successor
				} else {
					peer.setSuccessor2(Integer.parseInt(message[3])); //successor has not updated its neighbour yet, take its 2nd successor
				}
				peer.setTscSuc2(0);
				System.out.println("Peer is successor 2. It has timed out. New succ2 = " + peer.getSuccessor2());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
