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
		//System.out.println("UDPPingReceiver started");
		byte[] receiveBuf = new byte[512]; //buffer that incoming data will be stored in
		DatagramPacket pingReceive = new DatagramPacket(receiveBuf, 512); //Packet storing incoming packets
		DatagramSocket receiveSocket = peer.getUdpSocket(); //The listening socket
		Integer preTemp = null;
		Boolean preFlag = false;
		
		//Message loop
		while (true) {
			//System.out.println("Receiver waiting");
			try {
				receiveSocket.receive(pingReceive);
				String[] message = new String(pingReceive.getData(),0,pingReceive.getLength()).trim().split("\\s+");
				Integer from = Integer.parseInt(message[0]);
				//System.out.print("Packet received from peer " + from + " on port " + pingReceive.getPort() + ": "); 
				//System.out.println(message[1]);
				
				switch (message[1]) {
					case "request":
						System.out.println("A ping request message was received from Peer " + from);
						String pingResponse = (peer.getId() + " response " + message[2]);
						byte[] outPingResponse = pingResponse.getBytes();
						InetAddress requesterAddr = InetAddress.getByName("localhost");
						DatagramSocket pingResponseSocket = new DatagramSocket();
						DatagramPacket pingResponsePack = new DatagramPacket(outPingResponse, outPingResponse.length, requesterAddr, 50000 + from);
						pingResponseSocket.send(pingResponsePack);
						pingResponseSocket.close();
						if (preFlag) {
							//System.out.print("preFlag set");
							if (from == peer.getPredecessor1()) {
								//System.out.println(": RESET predecessor 2 (" + peer.getPredecessor2() + ") with " + preTemp);
								peer.setPredecessor2(preTemp);
							} else if (from == peer.getPredecessor2()) {
								//System.out.println(": RESET predecessor 1 (" + peer.getPredecessor1() + ") with " + preTemp);
								peer.setPredecessor1(preTemp);
							}
							preFlag = false;
						}
						
						if (peer.getPredecessor1() == -1) {
							//System.out.println("predecessor 1 not set...setting " + from);
							peer.setPredecessor1(from);
						} else if (peer.getPredecessor2() == -1) {
							//System.out.println("predecessor 2 not set...setting " + from);
							peer.setPredecessor2(from);
						} else if (from != peer.getPredecessor1() && from != peer.getPredecessor2()) {
							//System.out.println("new predecessor detected...saving " + from);
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
						System.out.print("A ping response message was received from Peer " + from);
						if (from == peer.getSuccessor1()) {
							peer.setTscSuc1(Integer.parseInt(message[2]));
							//System.out.println("Setting tscSuc1 to " + message[2]);
						} else if (from == peer.getSuccessor2()) {
							peer.setTscSuc2(Integer.parseInt(message[2]));
							//System.out.println("Setting tscSuc2 to " + message[2]);
						}
						break;
					/*	
					 *  reset tscSuccX for fromID = X
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
		 */
	}
}
