import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
		Integer destPort = peer.getSuccessor1();
		DatagramSocket succ = null;
		InetAddress addr = null;
		byte[] buf = (peer.getId().toString() + " request").getBytes();
		Integer length = buf.length;
		
		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		try {
			while (true) {
				System.out.println("Ping " + (50000 + destPort));
				succ = new DatagramSocket();
				DatagramPacket pingPacket = new DatagramPacket(buf, length, addr, 50000 + destPort);
				try {
					succ.send(pingPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Thread.sleep(5000);
				if (destPort == peer.getSuccessor1()) {
					destPort = peer.getSuccessor2();	
				} else {
					destPort = peer.getSuccessor1();
				}
				succ.close();
			}
		} catch (SocketException | InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// get next peer number
		// load datagram with ping message and address
		// Send ping (alternate which peer to send ping to)
		// wait(5000) (5 secs)
		

	}
	
}
