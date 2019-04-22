import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPFileReceiver implements Runnable {
	private Peer peer;
		
	UDPFileReceiver (Peer peer) {
		this.peer = peer;
	}
	
	@Override
	public void run() {
		DatagramSocket receiver = peer.getUdpFileRecSocket();
		byte[] buf = new byte[peer.getMSS()];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		Boolean transferComplete = false;
		while (!transferComplete) {
			try {
				receiver.receive(packet);
				
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		receiver.close();
		// listen for datagrams on unused port from Peer object
		// save sent data if applicable (if packet sequence is expected)
		// setup new datagram with return address socket
		// send ACK to sender
		// process saved data into file format ->export?
	}
}
