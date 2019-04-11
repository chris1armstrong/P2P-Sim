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
		System.out.println(peer.getId());
		try {
			tcpReceiver = new ServerSocket(50000 + peer.getId());
			while(true) {
				System.out.println("Socket bound, waiting for connection");
				Socket peerConnection = tcpReceiver.accept();
				InputStreamReader inReader = new InputStreamReader(peerConnection.getInputStream());
				BufferedReader inFromClient = new BufferedReader(inReader);
				String[] clientSentence = inFromClient.readLine().split("\\s+");
				System.out.println("Received: " + clientSentence + " |=");
				
				if (Integer.parseInt(clientSentence[0]) == peer.getId()) {
					System.out.println("I got it");
				} else {
					Socket nextPeer = new Socket("localhost",50002);
					DataOutputStream outToClient = new DataOutputStream(nextPeer.getOutputStream());
					byte[] b = new String("Sending from " + (50000+peer.getId())).getBytes();
					outToClient.write(b);
					nextPeer.close();
				}
				//capitalizedSentence = clientSentence.toUpperCase() + 'n';
				//outToClient.writeBytes(capitalizedSentence);
				
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
