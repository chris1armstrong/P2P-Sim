//Written by Chris Armstrong, April 2019

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
	public void run() { //setup receiver buffer, logfile etc
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
		
		while (!transferComplete) { //while there is still file left to receive
			try { // receive the incoming packet, generate log message
				receiver.receive(packet);
				eventTime = System.currentTimeMillis() - peer.getStartTime();
				event = "rcv";
				byte[] input = new byte[packet.getLength()];
				input = packet.getData();
				ByteBuffer data = ByteBuffer.wrap(input);
				Integer seqNum = data.getInt();
				length = packet.getLength() - 12;
				Integer ackNum = seqNum + length;
				
				if (length > 0) {
					writer.write(event + " " + eventTime + " " + seqNum + " " + length + " 0\n");
					writer.flush();
				}// generate response (ACK) message
				ByteBuffer respMessage = ByteBuffer.wrap(new byte[12]);
				respMessage.putInt(0);
				respMessage.putInt(ackNum);
				respMessage.putInt(length);
				byte[] outBuf = respMessage.array();
				DatagramPacket response = new DatagramPacket(outBuf, outBuf.length, packet.getSocketAddress());
				if (packet.getLength() == 12) { //length of header w/ no data
					System.out.println("The file is received");
					transferComplete = true;
				} else { //write data out to the receive file
					fileOut.write(Arrays.copyOfRange(input, 12, packet.getLength()));
				}
				receiver.send(response); // send response message
				eventTime = System.currentTimeMillis() - peer.getStartTime();
				event = "snd";
				//record send in log
				if (length > 0) {
					writer.write(event + " " + eventTime + " 0 " + length + " " + ackNum + "\n");
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		receiver.close();
	}
}
