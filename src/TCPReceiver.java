import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPReceiver implements Runnable {
	private Peer peer;
	
	public TCPReceiver(Peer peer) {
		this.peer = peer;
	}
	
	
	@Override
	public void run() {
		ServerSocket tcpReceiver = null;
		try {
			tcpReceiver = new ServerSocket(50000 + peer.getId()); //Bind TCP socket to this peer's tcpreceiver
			while(true) {
				if (peer.getRunning() == false) { //check if the peer is departing
					break;
				}
				//get a connection, establish inputs and output streams
				Socket peerConnection = tcpReceiver.accept();
				DataInputStream inStream = new DataInputStream(peerConnection.getInputStream());
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(inStream));
				DataOutputStream outToClient = new DataOutputStream(peerConnection.getOutputStream());
				//parse the tcp message
				String inputString = inFromClient.readLine();
				String[] message = inputString.split("\\s+");
				Integer from = Integer.parseInt(message[0]);
				//execute behaviour based on message
				switch (message[1]) {
				case "getSuccessors": //a peer has asked this peer for its successors
					String reply = new String(peer.getId() + " mySuccessors " + peer.getSuccessor1() + " " + peer.getSuccessor2());
					outToClient.writeBytes(reply + '\n');
					break;
				case "departing": //one of this peers successors is departing, updating successors
					if (from == peer.getSuccessor1()) {
						peer.setSuccessor1(Integer.parseInt(message[2]));
						peer.setSuccessor2(Integer.parseInt(message[3]));
						peer.setTscSuc1(0);
						peer.setSequenceNum1(0);
						peer.setTscSuc2(0);
						peer.setSequenceNum2(0);
						System.out.println("Peer " + from + " will depart from the network");
						System.out.println("My first successor is now peer " + peer.getSuccessor1());
						System.out.println("My second successor is now peer " + peer.getSuccessor2());
					} else if (from == peer.getSuccessor2()) {
						peer.setSuccessor2(Integer.parseInt(message[2]));
						peer.setTscSuc2(0);
						peer.setSequenceNum2(0);
						System.out.println("Peer " + from + " will depart from the network");
						System.out.println("My first successor is now peer " + peer.getSuccessor1());
						System.out.println("My second successor is now peer " + peer.getSuccessor2());
					}
					break;
				case "request": //a peer has made a file request to me
					Integer origin = Integer.parseInt(message[2]);
					Integer fileNum = Integer.parseInt(message[3]);
					Integer originFileReceiverPort = Integer.parseInt(message[4]);
					Integer locator = fileNum%255;
					Boolean mine = false; //checking if this peer has the file
					if (locator <= peer.getId() && locator > from) { 
						mine = true;
					} else if (locator > from && peer.getId() < from) {
						mine = true;
					} else if (locator < from && locator <= peer.getId() && peer.getId() < from){
						mine = true;
					} else { //this peer did NOT have the file, forward the request
						//Establish TCP connection to port 50000 + peer.getSuccessor1()
						InetAddress forwardAddr = InetAddress.getByName("localhost");
						Socket forward = new Socket(forwardAddr, 50000 + peer.getSuccessor1());
						DataOutputStream forwardOut = new DataOutputStream(forward.getOutputStream());
						String forwardMessage = new String(peer.getId() + " request " + origin + " " + message[3] + " " + originFileReceiverPort);
						//forward to immediate successor
						forwardOut.writeBytes(forwardMessage + '\n');
						forward.close();
						//Close TCP connection
						System.out.println("File " + message[3] + " is not stored here");
						System.out.println("File request message has been forwarded to my successor");
					}
					if (mine) { //This peer does have the file
						//Establish connection to origin TCP port, send confirmation
						System.out.println("File " + message[3] + " is here");
						InetAddress confirmAddr = InetAddress.getByName("localhost");
						Socket confirm = new Socket(confirmAddr, 50000 + origin);
						DataOutputStream forwardOut = new DataOutputStream(confirm.getOutputStream());
						String forwardMessage = new String(peer.getId() + " confirm " + message[3]);
						System.out.println("A response message, destined for peer " + origin + ", has been sent");
						forwardOut.writeBytes(forwardMessage + '\n');
						confirm.close();
						//Start UDPFileSender Thread, give it fileNo & UDPFileReceiverPort
						Integer receiverPort = Integer.parseInt(message[4]);
						Thread sender = new Thread(new UDPFileSender(peer, message[3], receiverPort));
						sender.start();
					}
					break;
				case "confirm":	//received confirmation message from peer holding requested fileNo
					System.out.println("Received a response message from peer " + Integer.parseInt(message[0]) + 
								", which has the file "+ Integer.parseInt(message[2]));
					break;
				}
				/*
				 * message types:
				 * 1. "fromID getSuccessors"
				 * 2. "fromID departing succ1 succ2"
				 * 3. "fromID request origin fileNo UDPFileReceiverPort"
				 * 4. "fromID confirm fileNo"
				 */
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			tcpReceiver.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
