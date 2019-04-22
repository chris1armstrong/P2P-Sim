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
	private Integer predecessor1;
	private Integer predecessor2;
	private Integer tscSuc1; //Time Since Contact
	private Integer tscSuc2; //Time Since Contact
	
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
		this.predecessor1 = -1;
		this.predecessor2 = -1;
		this.setTscSuc1(0);
		this.setTscSuc2(0);
	}
	

	public Integer getId() {
		return id;
	}

	public synchronized Integer getSuccessor1() {
		return successor1;
	}

	public synchronized Integer getSuccessor2() {
		return successor2;
	}

	public synchronized void setSuccessor1(Integer i) {
		this.successor1 = i;
	}

	public synchronized void setSuccessor2(Integer i) {
		this.successor2 = i;
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


	public synchronized Integer getPredecessor2() {
		return predecessor2;
	}


	public synchronized void setPredecessor2(Integer predecessor2) {
		this.predecessor2 = predecessor2;
	}


	public synchronized Integer getPredecessor1() {
		return predecessor1;
	}


	public synchronized void setPredecessor1(Integer predecessor1) {
		this.predecessor1 = predecessor1;
	}


	public synchronized Integer getTscSuc1() {
		return tscSuc1;
	}


	public synchronized void setTscSuc1(Integer l) {
		this.tscSuc1 = l;
	}


	public synchronized Integer getTscSuc2() {
		return tscSuc2;
	}


	public synchronized void setTscSuc2(Integer l) {
		this.tscSuc2 = l;
	}

}
