//Written by Chris Armstrong, April 2019

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
	public void run() { //setup log file and parse file to be sent
		String filename = fileNum + ".pdf";
		String logname = "responding_log.txt";
		File input = new File(filename);
		RandomAccessFile inputFile = null;
		BufferedWriter writer = null;
		String event = null;
		try {
			writer = new BufferedWriter(new FileWriter(logname));
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		Random random = new Random();
		try {
			inputFile = new RandomAccessFile(input,"r");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		System.out.println("We now start sending the file .........");
		//setup up datagram socket and sending buffer
		DatagramSocket succ = null;
		byte[] filebytes = new byte[peer.getMSS()];
		Integer dataLength = 0;
		try {
			Boolean done = false;
			Integer sequenceNum = 1;
			//setup socket ready for sending
			succ = new DatagramSocket();
			succ.setSoTimeout(1000);
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			while (!done) { //while the file has not been received in full
				dataLength = inputFile.read(filebytes); // read in the next chunk
			    if (dataLength == -1) { //catch the end of file, modify to keep sequence numbers consistent
			    	dataLength = 0;
			    	done = true;
			    } 
			    //setup header information
				Integer ackNumber = 0;
			    ByteBuffer bufferino = ByteBuffer.allocate(12);
			    bufferino.putInt(sequenceNum);
			    bufferino.putInt(ackNumber);
			    bufferino.putInt(peer.getMSS());
				byte[] seqBytes = bufferino.array();
				//combine the header and data into the packet
				byte[] buf = new byte[dataLength + seqBytes.length];
				System.arraycopy(seqBytes, 0, buf, 0, seqBytes.length);
				System.arraycopy(filebytes, 0, buf, seqBytes.length, dataLength);
				DatagramPacket filePacket = new DatagramPacket(buf, buf.length, addr, receiverPort);
				//prepare ACK receiver buffer & packet
				byte[] ackBuf = new byte[12];
				DatagramPacket ackPacket = new DatagramPacket(buf, ackBuf.length);
				//setup this packet's expected ACK number
				Boolean response = false;
				Integer expectedACK = sequenceNum + dataLength;
				Integer retrans = 0;
				while(!response) { //loop until the correct ACK is received
					try {
						//calculate drop rate here, and log message
						if (random.nextDouble() >= peer.getDropRate()) {
							succ.send(filePacket);
							if (retrans > 0) {
								event = "RTX";
							} else {
								event = "snd";
							}
						} else {
							event = "drop";
							if (retrans > 0) {
								event = "RTX/drop";
							}
							retrans = 1;
						}
						Long eventTime = System.currentTimeMillis() - peer.getStartTime();
						if (!done) {
							writer.write(event + " " + eventTime + " " + sequenceNum + " " + dataLength + " 0\n");
							writer.flush();
						}
						//wait to receive the ACK packet, then generate log message
						succ.receive(ackPacket);
						eventTime = System.currentTimeMillis() - peer.getStartTime();
						event = "rcv";
						ackBuf = ackPacket.getData();
						ByteBuffer wrappedAckBuf = ByteBuffer.wrap(ackBuf);
						Integer tempSeqNum = wrappedAckBuf.getInt();
						ackNumber = wrappedAckBuf.getInt();
						Integer tempLength = wrappedAckBuf.getInt();
						if (!done) {
							writer.write(event + " " + eventTime + " " + tempSeqNum + " " + tempLength + " " + ackNumber + "\n");
							writer.flush();
						}
						if (ackNumber.equals(expectedACK)) { // packet was successfully received, setup next packet sequence number
							response = true;
							sequenceNum = ackNumber;
						}
					} catch (SocketTimeoutException e) {//catch the timeout to allow the loop to continue
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
