import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;

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
		Random random = new Random();
		try {
			inputFile = new RandomAccessFile(input,"r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//System.out.println("Peer: " + peer.getId() + " file sender thread started");
		//System.out.println("SLURPING UP the file: " + filename + " === opened file: " + input);
		//System.out.println("Chunking and sending to: " + receiverPort);
		System.out.println("We now start sending the file .........");
		
		DatagramSocket succ = null;
		byte[] filebytes = new byte[peer.getMSS()];
		Integer dataLength = 0;
		try {
			Boolean done = false;
			Integer sequenceNum = 0;

			succ = new DatagramSocket();
			succ.setSoTimeout(1000);
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			while (!done) {
				dataLength = inputFile.read(filebytes);
				//System.out.println("I read in " + dataLength + " bytes");
			    if (dataLength == -1) { //catch the end of file, modify to keep sequence numbers consistent
					//System.out.println("I read nothing, file end");
			    	dataLength = 0;
			    	done = true;
			    } 
			    
			    ByteBuffer bufferino = ByteBuffer.allocate(4).putInt(sequenceNum);
				byte[] seqBytes = bufferino.array();
				//System.out.println("buf length = " + (dataLength + seqBytes.length));
				byte[] buf = new byte[dataLength + seqBytes.length];
				System.arraycopy(seqBytes, 0, buf, 0, seqBytes.length);
				System.arraycopy(filebytes, 0, buf, seqBytes.length, dataLength);
				DatagramPacket filePacket = new DatagramPacket(buf, buf.length, addr, receiverPort);
				
				byte[] ackBuf = new byte[4];
				DatagramPacket ackPacket = new DatagramPacket(buf, ackBuf.length);
				
				Boolean response = false;
				Integer expectedACK = sequenceNum + dataLength;
				while(!response) {
					try {

						//System.out.println("sending file packet " + sequenceNum);
						//calculate drop rate here
						if (random.nextDouble() >= peer.getDropRate()) {
							succ.send(filePacket);
						}
						succ.receive(ackPacket);
						ackBuf = ackPacket.getData();
						Integer ackNumber = ByteBuffer.wrap(ackBuf).getInt();
						//System.out.println("received ackNumber: " + ackNumber + " === expected: " + expectedACK);
						if (ackNumber.equals(expectedACK)) {
							//System.out.println("Received expected ACK, updating seqNum = " + ackNumber);
							response = true;
							sequenceNum = ackNumber;
						}
					} catch (SocketTimeoutException e) {
						//System.out.println("response timeout for packet with seq num: " + sequenceNum + " === resending");
					}
				}
				if (done) {
					System.out.println("The file is sent");
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			succ.close();
		}
		
	}
}
