
public class UDPPing implements Runnable {
	private Peer peer;
		
	UDPPing (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {

		// Set up datagram
		// get peerNum and set up send data
		// set timeout value on socket
		// try 	
		//		Send ping (alternate which peer to send ping to)
		// 		wait for response
		// catch timeout
		//		increment unanswered
		// finally
		//		if unanswered > 3
		//			notify? update?
		//			
		

	}
	
}
