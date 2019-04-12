
public class UDPFileReceiver implements Runnable {
	private Peer peer;
		
	UDPFileReceiver (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {
		// listen for datagrams on unused port from Peer object
		// save sent data if applicable (if packet sequence is expected)
		// setup new datagram with return address socket
		// send ACK to sender
		// process saved data into file format ->export?
	}
}
