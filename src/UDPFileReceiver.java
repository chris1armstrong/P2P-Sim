import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
		byte[] buf = new byte[peer.getMSS() + 12];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		Boolean transferComplete = false;
		FileOutputStream fileOut = null;
		String logname = "requesting_log.txt";
		BufferedWriter writer = null;
		String event = null;
		Long eventTime = 0L;
		Integer length = 0;
		try {
			writer = new BufferedWriter(new FileWriter(logname));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			fileOut = new FileOutputStream("received.pdf");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		while (!transferComplete) {
			System.out.println("waiting for file packet");
			try {
				receiver.receive(packet);
				eventTime = System.currentTimeMillis() - peer.getStartTime();
				event = "rcv";
				byte[] input = new byte[packet.getLength()];
				input = packet.getData();
				ByteBuffer data = ByteBuffer.wrap(input);
				Integer seqNum = data.getInt();
				System.out.println("Received packet seq= " + seqNum + " === received packet length = " + packet.getLength());
				length = packet.getLength() - 12;
				Integer ackNum = seqNum + length;
				
				writer.write(event + " " + eventTime + " " + seqNum + " " + length + " 0\n");
				writer.flush();
				//System.out.println("ackNum= " + ackNum);
				
				ByteBuffer respMessage = ByteBuffer.wrap(new byte[12]);
				respMessage.putInt(0);
				respMessage.putInt(ackNum);
				respMessage.putInt(peer.getMSS());
				byte[] outBuf = respMessage.array();
				DatagramPacket response = new DatagramPacket(outBuf, outBuf.length, packet.getSocketAddress());
				if (packet.getLength() == 12) { //length of lone sequence Num
					System.out.println("The file is received");
					transferComplete = true;
				} else {
					fileOut.write(Arrays.copyOfRange(input, 4, packet.getLength()));;
				}
				receiver.send(response);
				eventTime = System.currentTimeMillis() - peer.getStartTime();
				event = "snd";
				writer.write(event + " " + eventTime + " 0 0 " + ackNum + "\n");
				writer.flush();
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
