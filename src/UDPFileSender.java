import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPFileSender implements Runnable {
	private String fileNum;
	private Integer receiverSocket;
	private Peer peer;
		
	UDPFileSender (Peer peer, String fileNum, Integer receiverSocket) {
		this.fileNum = fileNum;
		this.receiverSocket = receiverSocket;
		this.peer = peer;
	}
	
	@Override
	public void run() {
		String filename = fileNum + ".pdf";
		File input = new File(filename);
		System.out.println("Peer: " + peer.getId() + " file sender thread started");
		System.out.println("SLURPING UP the file: " + filename + " === opened file: " + input);
		System.out.println("Chunking and sending to: " + receiverSocket);

		DatagramSocket succ = null; //Ping sent on this socket
		InetAddress addr = null; //Address of this machine
		byte[] buf; //ping message in buffer
		try {
			addr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		/*
		succ = new DatagramSocket();
		buf = (peer.getId().toString() + " request " + seq).getBytes();
		DatagramPacket pingPacket = new DatagramPacket(buf, buf.length, addr, 50000 + destPort);
		*/
		// get File using fileNum
		// process the file into chunks of size peer.getMSS()
		// setup new datagram with address of receiverSocket & sequence number (1 or 0)
		// stop and wait protocol
			// if ACK correctly received, send next chunk
			// if ACK not received within timeout window, re-send chunk (Don't need to worry about duplicate acks)
		// if file transfer complete, send DONE packet and wait for ACK
		// re-send DONE packet is ACK timeout
		// kill/close thread
	}
}
