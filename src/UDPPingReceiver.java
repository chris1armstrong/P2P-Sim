import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP Receiver which listens on the port associated with its peer (50000 + peer's id)<p>
 * Will respond to ping requests and will maintain the peer's predecessor record
 */
public class UDPPingReceiver implements Runnable {
	/**
	 * The peer object associated with this receiver
	 */
	private Peer peer;
	
	/**
	 * UDPPingReceiver constructor
	 * @param peer the peer associated with this receiver
	 */
	UDPPingReceiver (Peer peer) {
		this.peer = peer;
	}

	/**
	 * This method begins listening on the socket and handles UDP messages sent to this peer
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("UDPPingReceiver started");
		byte[] receiveBuf = new byte[512]; //buffer that incoming data will be stored in
		DatagramPacket pingReceive = new DatagramPacket(receiveBuf, 512); //Packet storing incoming packets
		DatagramSocket receiveSocket = peer.getUdpSocket(); //The listening socket
		Integer preTemp = null;
		Boolean preFlag = false;
		
		//Message loop
		while (true) {
			System.out.println("Receiver waiting");
			try {
				receiveSocket.receive(pingReceive);
				String[] message = new String(pingReceive.getData(),0,pingReceive.getLength()).trim().split("\\s+");
				Integer from = Integer.parseInt(message[0]);
				System.out.print("Packet received from peer " + from + " on port " + pingReceive.getPort() + ": "); 
				System.out.println(message[1]);
				
				switch (message[1]) {
					case "request":
						String pingResponse = (peer.getId() + " response");
						byte[] outPingResponse = pingResponse.getBytes();
						InetAddress requesterAddr = InetAddress.getByName("localhost");
						DatagramSocket pingResponseSocket = new DatagramSocket();
						DatagramPacket pingResponsePack = new DatagramPacket(outPingResponse, outPingResponse.length, requesterAddr, 50000 + from);
						pingResponseSocket.send(pingResponsePack);
						
						if (preFlag) {
							System.out.print("preFlag set");
							if (from == peer.getPredecessor1()) {
								System.out.println(": RESET predecessor 2 (" + peer.getPredecessor2() + ") with " + preTemp);
								peer.setPredecessor2(preTemp);
							} else if (from == peer.getPredecessor2()) {
								System.out.println(": RESET predecessor 1 (" + peer.getPredecessor1() + ") with " + preTemp);
								peer.setPredecessor1(preTemp);
							}
							preFlag = false;
						}
						
						if (peer.getPredecessor1() == -1) {
							System.out.println("predecessor 1 not set...setting " + from);
							peer.setPredecessor1(from);
						} else if (peer.getPredecessor2() == -1) {
							System.out.println("predecessor 2 not set...setting " + from);
							peer.setPredecessor2(from);
						} else if (from != peer.getPredecessor1() && from != peer.getPredecessor2()) {
							System.out.println("new predecessor detected...saving " + from);
							preTemp = from;
							preFlag = true;
						}
						break;
					/*	
					 *  send response to Port 50000 + fromID
					 * 	if (preFlag = 1) replace other preID with preTemp, preFlag = 0
					 * 	if (fromID != either preID) store preTemp, set preFlag = 1
					 * 	break;
					 */
					case "response":
						System.out.print("Ping response from " + from + ". ");
						if (from == peer.getSuccessor1()) {
							peer.setTscSuc1(System.currentTimeMillis());
							System.out.println("Resetting tscSuc1");
						} else if (from == peer.getSuccessor2()) {
							peer.setTscSuc2(System.currentTimeMillis());
							System.out.println("Resetting tscSuc2");
						}
						break;
					/*	
					 *  reset tscSuccX for fromID = X
					 * 	break;
					 */
					case "leaving":
						System.out.print("Peer " + from + " leaving. ");
						if (from == peer.getSuccessor1()) {
							peer.setSuccessor1(Integer.parseInt(message[2]));
							peer.setSuccessor2(Integer.parseInt(message[3]));
							peer.setTscSuc1(System.currentTimeMillis());
							peer.setTscSuc2(System.currentTimeMillis());
							System.out.println("Peer is successor 1. New succ1 = " + peer.getSuccessor1() + ". New succ2 = " + peer.getSuccessor2());
						} else if (from == peer.getSuccessor2()) {
							peer.setSuccessor2(Integer.parseInt(message[2]));
							peer.setTscSuc2(System.currentTimeMillis());
							System.out.println("Peer is successor 2. New succ2 = " + peer.getSuccessor2());
						}
						break;
					/*	
					 * 	if (fromID == peer.succ1) peer.succ1 = succ1, peer.succ2 = succ2, reset tscSuccX for both
					 * 	else if (fromID == peer.succ2) peer.succ2 = succ1, reset tscSucc2
					 * 	break;
					 */
					case "getSuccessors":
						System.out.println("Peer " + from + " asking for my successors");
						String successorsResponse = (peer.getId() + " mySuccessors " + peer.getSuccessor1() + " " + peer.getSuccessor2());
						byte[] out = successorsResponse.getBytes();
						InetAddress addr = InetAddress.getByName("localhost");
						DatagramSocket resp = new DatagramSocket();
						DatagramPacket pack = new DatagramPacket(out, out.length, addr, 50000 + from);
						resp.send(pack);
						break;
					/*	
					 * 	send "peer.id mySuccessors peer.succ1 peer.succ2" to Port 50000 + fromID
					 * 	break;
					 */
					case "mySuccessors":
						System.out.println("Peer " + from + " telling me its successors");
						Long timeout = 30000L; //30 seconds = 30000 milliseconds
						if ((System.currentTimeMillis() - peer.getTscSuc1()) > timeout) {
							peer.setSuccessor1(peer.getSuccessor2());
							peer.setSuccessor2(Integer.parseInt(message[2]));
							peer.setTscSuc1(System.currentTimeMillis());
							peer.setTscSuc2(System.currentTimeMillis());
							System.out.println("Peer is successor 1. It has timed out. New succ1 = " + peer.getSuccessor1() + ". New succ2 = " + peer.getSuccessor2());
						} else if ((System.currentTimeMillis() - peer.getTscSuc2()) > timeout) {
							peer.setSuccessor2(Integer.parseInt(message[3]));
							peer.setTscSuc2(System.currentTimeMillis());
							System.out.println("Peer is successor 2. It has timed out. New succ2 = " + peer.getSuccessor2());
						}
						break;
					/*	
					 * 	if (tscSucc1 > 30 sec) peer.succ1 = peer.succ2 = succ1, peer.succ2 = succ2, reset tscSuccX
					 * 	else if (tscSucc2 > 30 sec) peer.succ2 = succ1, reset tscSucc2
					 * 	break;
					 */
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * message types:
		 * 1. "fromID request"
		 * 2. "fromID response"
		 * 3. "fromID leaving succ1 succ2"
		 * 4. "fromID getSuccessors"
		 * 5. "fromID mySuccessors peer.succ1 peer.succ2
		 */
	}
	
	/*
	private void SavePredecessor(Integer peer) {
		if (order == null) {
			predecessor1 = peer;
			order = 1;
		} else if (predecessor2 == null) {
			predecessor2 = peer;
		} else if (peer != predecessor1 || peer != predecessor2) {
			if (order == 1) { //1 indicates predecessor1 is older
				predecessor1 = peer;
			} else {		//else order = 2, predecessor2 is older
				predecessor2 = peer;
			}
		}
		
	}*/
}
