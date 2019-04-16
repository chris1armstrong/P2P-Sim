import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Peer {
	private Integer id;
	private Integer successor1;
	private Integer successor2;
	private Integer MSS;
	private Float dropRate;
	private DatagramSocket udpSocket;
	private Socket tcpClientSocket;
	private ServerSocket tcpServerSocket;
	
	public Peer(String[] args) {
		this.id = Integer.parseInt(args[0]);
		this.successor1 = Integer.parseInt(args[1]);
		this.successor2 = Integer.parseInt(args[2]);
		this.MSS = Integer.parseInt(args[3]);
		this.dropRate = Float.parseFloat(args[4]);
		try {
			this.udpSocket = new DatagramSocket(50000 + this.id);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public Integer getId() {
		return id;
	}

	public Integer getSuccessor1() {
		return successor1;
	}

	public Integer getSuccessor2() {
		return successor2;
	}

	public Integer getMSS() {
		return MSS;
	}

	public Float getDropRate() {
		return dropRate;
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}

	public Socket getTcpClientSocket() {
		return tcpClientSocket;
	}

	public ServerSocket getTcpServerSocket() {
		return tcpServerSocket;
	}

}
