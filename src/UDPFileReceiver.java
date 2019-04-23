import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream("received.pdf");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		while (!transferComplete) {
			try {
				receiver.receive(packet);
				byte[] input = new byte[packet.getLength()];
				input = packet.getData();
				ByteBuffer data = ByteBuffer.wrap(input);
				Integer seqNum = data.getInt();
				Integer ackNum = seqNum + packet.getLength() - 4;
				ByteBuffer respMessage = ByteBuffer.wrap(new byte[4]).putInt(ackNum);
				byte[] outBuf = respMessage.array();
				DatagramPacket response = new DatagramPacket(outBuf, outBuf.length, packet.getSocketAddress());
				if (packet.getLength() == 4) { //length of lone sequence Num
					transferComplete = true;
				} else {
					fileOut.write(Arrays.copyOfRange(input, 4, packet.getLength()));;
				}
				receiver.send(response);
			} catch (IOException e) {
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
