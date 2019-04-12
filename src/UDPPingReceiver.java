
public class UDPPingReceiver implements Runnable {
	private Peer peer;
		
	UDPPingReceiver (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {
		// listen for connection on port num 50000 + peerNum
		// extract predecessor from ping
		// save latest two predecessor numbers
		// setup new datagram and return address socket
		// Send ping ACK

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
