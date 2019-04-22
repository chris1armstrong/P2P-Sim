import java.io.*;
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
		//tcpReceiver.setReuseAddress(true);
		System.out.println(peer.getId());
		try {
			tcpReceiver = new ServerSocket(50000 + peer.getId());
			while(true) {
				System.out.println("TCPSocket bound, waiting for connection");
				Socket peerConnection = tcpReceiver.accept();
				DataInputStream inStream = new DataInputStream(peerConnection.getInputStream());
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(inStream));
				
				DataOutputStream outToClient = new DataOutputStream(peerConnection.getOutputStream());
				
				String inputString = inFromClient.readLine();
				String[] clientSentence = inputString.split("\\s+");
				System.out.println("Received: " + inputString + " |=");
				
				if (clientSentence[1].equals("getSuccessors")) {
					System.out.println("Successors requested by peer " + clientSentence[0]);
					String reply = new String(peer.getId() + " mySuccessors " + peer.getSuccessor1() + " " + peer.getSuccessor2());
					outToClient.writeBytes(reply + '\n');
				} else if (Integer.parseInt(clientSentence[0]) == peer.getId()) {
					System.out.println("I got it");
				} /*else {
					Socket nextPeer = new Socket("localhost",50000 + peer.getSuccessor1());
					DataOutputStream outage = new DataOutputStream(nextPeer.getOutputStream());
					byte[] b = new String("Sending from " + (50000+peer.getId())).getBytes();
					outage.write(b);
					nextPeer.close();
				}*/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			tcpReceiver.close();
			System.out.println("I've closed up");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
