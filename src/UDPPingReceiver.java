import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
		byte[] receiveBuf = new byte[1024];
		DatagramPacket pingReceive = new DatagramPacket(receiveBuf, 1024);
		DatagramSocket receiveSocket = peer.getUdpSocket();
		while (true) {
			System.out.println("Receiver waiting");
			try {
				receiveSocket.receive(pingReceive);
				//process received packet
				System.out.println(new String(pingReceive.getData()).trim());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// extract predecessor from ping
			// save latest two predecessor numbers
			// setup new datagram and return address socket
			// Send ping ACK
		}
		
		/*
		 * message types:
		 * 1. "fromID request"
		 * 2. "fromID response"
		 * 3. "fromID leaving succ1 succ2"
		 * 4. "fromID getSuccessors"
		 * 5. "fromID mySuccessors peer.succ1 peer.succ2
		 * 
		 * while (true) {
		 * 		listen on peer.UDPSocket
		 * 		parse message
		 * 		switch message[1]:
		 * 		case "request":
		 * 			send response to Port 50000 + fromID
		 * 			if (preFlag = 1) replace other preID with preTemp, preFlag = 0
		 * 			if (fromID != either preID) store preTemp, set preFlag = 1
		 * 			break;
		 * 		case "response":
		 * 			reset tscSuccX for fromID = X
		 * 			break;
		 * 		case "leaving":
		 * 			if (fromID == peer.succ1) peer.succ1 = succ1, peer.succ2 = succ2, reset tscSuccX for both
		 * 			else if (fromID == peer.succ2) peer.succ2 = succ1, reset tscSucc2
		 * 			break;
		 * 		case "getSuccessors":
		 * 			send "peer.id mySuccessors peer.succ1 peer.succ2" to Port 50000 + fromID
		 * 			break;
		 * 		case "mySuccessors":
		 * 			if (tscSucc1 > 30 sec) peer.succ1 = peer.succ2 = succ1, peer.succ2 = succ2, reset tscSuccX
		 * 			else if (tscSucc2 > 30 sec) peer.succ2 = succ1, reset tscSucc2
		 * 			break;
		 * }
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
