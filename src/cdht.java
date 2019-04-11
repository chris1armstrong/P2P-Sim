import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class cdht {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*int i = 0;
		for (String s : args) {
			System.out.println(i + ": " + s);
			i++;
		}*/
		Peer peer = new Peer(args);
		Thread tcpreceiver = new Thread(new TCPReceiver(peer));
		tcpreceiver.start();
		
		Boolean breaker = true;
		Scanner scan = new Scanner(System.in);
		while(breaker) {
			String input = scan.nextLine();
			String[] command = input.split("\\s+");
			if (command[0].equals("quit")) {
				System.out.println("I'm ded");
				breaker = false;
			} else if (command[0].equals("File")) {
				Integer fileNo = Integer.parseInt(command[1]);
				System.out.println("File num = " + fileNo);
				Integer fileLoc = fileNo%255;
				Socket request = null;
				try {
					request = new Socket("localhost", 50000 + fileLoc);
					DataOutputStream outToClient = new DataOutputStream(request.getOutputStream());
					byte[] b = new String(fileNo + " " + (50000+peer.getId())).getBytes();
					outToClient.write(b);
					request.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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


