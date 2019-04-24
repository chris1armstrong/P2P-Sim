import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPPingReceiver implements Runnable {
	private Peer peer;
	
	UDPPingReceiver (Peer peer) {
		this.peer = peer;
	}

	@Override
	public void run() {
		byte[] receiveBuf = new byte[512]; //buffer that incoming data will be stored in, arbitrarily large
		DatagramPacket pingReceive = new DatagramPacket(receiveBuf, 512); //Packet storing incoming packets
		DatagramSocket receiveSocket = peer.getUdpSocket(); //The listening socket
		Integer preTemp = null; //used to track predecessors
		Boolean preFlag = false;//used to track predecessors
		
		//Message loop
		while (true) {
			try {//receive the ping and parse the message
				receiveSocket.receive(pingReceive);
				String[] message = new String(pingReceive.getData(),0,pingReceive.getLength()).trim().split("\\s+");
				Integer from = Integer.parseInt(message[0]);
				
				switch (message[1]) {
					case "request": // received ping request, send a response message
						System.out.println("A ping request message was received from Peer " + from);
						String pingResponse = (peer.getId() + " response " + message[2]);
						byte[] outPingResponse = pingResponse.getBytes();
						InetAddress requesterAddr = InetAddress.getByName("localhost");
						DatagramSocket pingResponseSocket = new DatagramSocket();
						DatagramPacket pingResponsePack = new DatagramPacket(outPingResponse, outPingResponse.length, requesterAddr, 50000 + from);
						pingResponseSocket.send(pingResponsePack);
						pingResponseSocket.close();
						if (preFlag) { // update recorded predecessors with new contact
							if (from == peer.getPredecessor1()) {
								peer.setPredecessor2(preTemp);
							} else if (from == peer.getPredecessor2()) {
								peer.setPredecessor1(preTemp);
							}
							preFlag = false;
						}
						
						if (peer.getPredecessor1() == -1) { //save first predecessor
							peer.setPredecessor1(from);
						} else if (peer.getPredecessor2() == -1) { //save second predecessor
							peer.setPredecessor2(from);
						} else if (from != peer.getPredecessor1() && from != peer.getPredecessor2()) {
							preTemp = from; //this is a new predecessor, save until the next ping
							preFlag = true; //this new peer and the next ping requester are the current predecessors
						}
						break;
					case "response": //received response to a ping, reset TimeSinceContact value, used for timeout calculation
						System.out.println("A ping response message was received from Peer " + from);
						if (from == peer.getSuccessor1()) { 
							peer.setTscSuc1(Integer.parseInt(message[2]));
						} else if (from == peer.getSuccessor2()) {
							peer.setTscSuc2(Integer.parseInt(message[2]));
						}
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * message types:
		 * 1. "fromID request"
		 * 2. "fromID response"
		 */
	}
}
