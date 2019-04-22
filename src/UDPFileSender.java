import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;

public class UDPFileSender implements Runnable {
	private String fileNum;
	private Integer receiverPort;
	private Peer peer;
		
	UDPFileSender (Peer peer, String fileNum, Integer receiverPort) {
		this.fileNum = fileNum;
		this.receiverPort = receiverPort;
		this.peer = peer;
	}
	
	@Override
	public void run() {
		String filename = fileNum + ".pdf";
		File input = new File(filename);
		RandomAccessFile inputFile = null;
		try {
			inputFile = new RandomAccessFile(input,"r");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("Peer: " + peer.getId() + " file sender thread started");
		System.out.println("SLURPING UP the file: " + filename + " === opened file: " + input);
		System.out.println("Chunking and sending to: " + receiverPort);
		
		byte[] filebytes = new byte[peer.getMSS()];
		Integer dataLength = 0;
		try {
			Boolean done = false;
			Integer sequenceNum = 0;

			DatagramSocket succ = null;
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			while (!done) {
				dataLength = inputFile.read(filebytes);
			    if (dataLength == -1) {
					//System.out.println("writing range " + i + " to " + (f_length - 1));
			    	done = true;
			    } else {
			    	ByteBuffer bufferino = ByteBuffer.allocate(4).putInt(sequenceNum);
					byte[] seqBytes = bufferino.array();
					byte[] buf = new byte[dataLength + seqBytes.length];
					System.arraycopy(seqBytes, 0, buf, 0, seqBytes.length);
					System.arraycopy(filebytes, 0, buf, seqBytes.length, filebytes.length);
					DatagramPacket pingPacket = new DatagramPacket(buf, buf.length, addr, receiverPort);
					
					/*
					 * send packet
					 * wait for 0.5 seconds for reply with correct sequence number
					 * if (timeout) {
					 * 		resend same packet
					 * } else (response received) {
					 * 		update sequenceNum
					 * }
					 */
					sequenceNum = sequenceNum + dataLength;
			    }
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//Finally send complete signal
		
		/*
		succ = new DatagramSocket();
		buf = (peer.getId().toString() + " request " + seq).getBytes();
		DatagramPacket pingPacket = new DatagramPacket(buf, buf.length, addr, 50000 + destPort);
		*/
		// get File using fileNum
		// process the file into chunks of size peer.getMSS()
		// setup new datagram with sequence number (1 or 0) and byte[MSS]
		// stop and wait protocol
			// if ACK correctly received, send next chunk
			// if ACK not received within timeout window, re-send chunk (Don't need to worry about duplicate acks)
		// if file transfer complete, send DONE packet and wait for ACK
		// re-send DONE packet is ACK timeout
		// kill/close thread
	}
}
