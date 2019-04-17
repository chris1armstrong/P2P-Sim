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
				System.out.print("Packet received from " + pingReceive.getPort() + ": ");
				String[] message = new String(pingReceive.getData()).trim().split("\\s+");
				System.out.println(message);
				Integer from = Integer.parseInt(message[0]);
				
				switch (message[1]) {
					case "request":
						String pingResponse = (peer.getId() + " response");
						byte[] outPingResponse = pingResponse.getBytes();
						InetAddress requesterAddr = InetAddress.getByName("localhost");
						DatagramSocket pingResponseSocket = new DatagramSocket();
						DatagramPacket pingResponsePack = new DatagramPacket(outPingResponse, outPingResponse.length, requesterAddr, 50000 + from);
						pingResponseSocket.send(pingResponsePack);
						
						if (preFlag) {
							if (from == peer.getPredecessor1()) {
								peer.setPredecessor2(preTemp);
							} else if (from == peer.getPredecessor2()) {
								peer.setPredecessor1(preTemp);
							}
							preFlag = false;
						}
						
						if (peer.getPredecessor1() == -1) {
							peer.setPredecessor1(from);
						} else if (peer.getPredecessor2() == -1) {
							peer.setPredecessor2(from);
						} else if (from != peer.getPredecessor1() && from != peer.getPredecessor2()) {
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
						if (from == peer.getSuccessor1()) {
							peer.setTscSuc1(System.currentTimeMillis());
						} else if (from == peer.getSuccessor2()) {
							peer.setTscSuc2(System.currentTimeMillis());
						}
						break;
					/*	
					 *  reset tscSuccX for fromID = X
					 * 	break;
					 */
					case "leaving":
						if (from == peer.getSuccessor1()) {
							peer.setSuccessor1(Integer.parseInt(message[2]));
							peer.setSuccessor2(Integer.parseInt(message[3]));
							peer.setTscSuc1(System.currentTimeMillis());
							peer.setTscSuc2(System.currentTimeMillis());
						} else if (from == peer.getSuccessor2()) {
							peer.setSuccessor2(Integer.parseInt(message[2]));
							peer.setTscSuc2(System.currentTimeMillis());
						}
						break;
					/*	
					 * 	if (fromID == peer.succ1) peer.succ1 = succ1, peer.succ2 = succ2, reset tscSuccX for both
					 * 	else if (fromID == peer.succ2) peer.succ2 = succ1, reset tscSucc2
					 * 	break;
					 */
					case "getSuccessors":
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
						Long timeout = 30000L; //30 seconds = 30000 milliseconds
						if ((System.currentTimeMillis() - peer.getTscSuc1()) > timeout) {
							peer.setSuccessor1(Integer.parseInt(message[2]));
							peer.setSuccessor2(Integer.parseInt(message[3]));
							peer.setTscSuc1(System.currentTimeMillis());
							peer.setTscSuc2(System.currentTimeMillis());
						} else if ((System.currentTimeMillis() - peer.getTscSuc2()) > timeout) {
							peer.setSuccessor2(Integer.parseInt(message[2]));
							peer.setTscSuc2(System.currentTimeMillis());
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
