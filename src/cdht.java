import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class cdht {
	
	public static void main(String[] args) {

		Peer peer = new Peer(args);
		Thread tcpreceiver = new Thread(new TCPReceiver(peer));
		Thread udppingrec = new Thread(new UDPPingReceiver(peer));
		Thread udpping = new Thread(new UDPPing(peer));
		
		
		tcpreceiver.start();
		
		udppingrec.start();
		udpping.start();
		
		
		Boolean breaker = true;
		Scanner scan = new Scanner(System.in);
		while(breaker) {
			String input = scan.nextLine();
			String[] command = input.split("\\s+");
			if (command[0].equals("quit")) {
				System.out.println("I'm ded");
				breaker = false;
			} else if (command[0].equals("request")) {
				String fileNo = command[1];
				System.out.println("File num = " + fileNo);
				Socket request = null;
				
				try {
					peer.setUdpFileRecSocket(new DatagramSocket());
					Thread udpfilereceiver = new Thread(new UDPFileReceiver(peer));
					udpfilereceiver.start();
					
					request = new Socket("localhost", 50000 + peer.getSuccessor1());
					DataOutputStream outToClient = new DataOutputStream(request.getOutputStream());
					String b = new String(peer.getId() + " request " + peer.getId() + " " + fileNo + " " + peer.getUdpFileRecSocket().getPort());
					//message style: "fromID request origin fileNo UDPFileReceiverPort"
					outToClient.writeBytes(b + '\n');
					request.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		scan.close();
		// start receiver thread;
		// start ping threads; one for each successor
		// wait for command line input
		// 	 quit -> send message to predecessors
		//   get file -> setup tcp to appropriate successor
		//		wait for response containing peer with file
		//		setup UDP exchange, get and receive.
	}

}


