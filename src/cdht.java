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
				try {
					if (peer.getPredecessor1() != -1) {
						Socket request = null;
						request = new Socket("localhost", 50000 + peer.getPredecessor1());
						DataOutputStream requestOut = new DataOutputStream(request.getOutputStream());
						//"fromID departing succ1 succ2"
						String b = new String(peer.getId() + " departing " + peer.getSuccessor1() + " " + peer.getSuccessor2());
						requestOut.writeBytes(b + '\n');
						request.close();
						breaker = false;
					} 
					if (peer.getPredecessor2() != -1) {
						Socket request = null;
						request = new Socket("localhost", 50000 + peer.getPredecessor2());
						DataOutputStream requestOut = new DataOutputStream(request.getOutputStream());
						String b = new String(peer.getId() + " departing " + peer.getSuccessor1() + " " + peer.getSuccessor2());
						requestOut.writeBytes(b + '\n');
						request.close();
						breaker = false;
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				breaker = false;
				peer.setRunning(false);
				
			} else if (command[0].equals("request")) {
				String fileNo = command[1];
				Socket request = null;
				
				try {
					peer.setUdpFileRecSocket(new DatagramSocket());
					Thread udpfilereceiver = new Thread(new UDPFileReceiver(peer));
					udpfilereceiver.start();
					
					request = new Socket("localhost", 50000 + peer.getSuccessor1());
					DataOutputStream outToClient = new DataOutputStream(request.getOutputStream());
					//message style: "fromID request origin fileNo UDPFileReceiverPort"
					String b = new String(peer.getId() + " request " + peer.getId() + " " + fileNo + " " + peer.getUdpFileRecSocket().getLocalPort());
					System.out.println("File request message for " + fileNo + " has been sent to my successor");
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
	}
}


