import java.net.DatagramSocket;
import java.net.SocketException;

public class Peer {
	private Integer id;
	private Integer successor1;
	private Integer successor2;
	private Integer MSS;
	private Double dropRate;
	private DatagramSocket udpSocket;
	private DatagramSocket udpFileRecSocket;
	private Integer predecessor1;
	private Integer predecessor2;
	private Integer tscSuc1; //Time Since Contact
	private Integer tscSuc2; //Time Since Contact
	private Integer sequenceNum1;
	private Integer sequenceNum2;
	private Boolean running;
	private Long startTime;
	
	public Peer(String[] args) {
		this.id = Integer.parseInt(args[0]);
		this.successor1 = Integer.parseInt(args[1]);
		this.successor2 = Integer.parseInt(args[2]);
		this.MSS = Integer.parseInt(args[3]);
		this.dropRate = Double.parseDouble(args[4]);
		try {
			this.udpSocket = new DatagramSocket(50000 + this.id);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.predecessor1 = -1;
		this.predecessor2 = -1;
		this.setTscSuc1(0);
		this.setTscSuc2(0);
		this.sequenceNum1 = 0;
		this.sequenceNum2 = 0;
		this.setUdpFileRecSocket(null);
		this.setRunning(true);
		this.startTime = System.currentTimeMillis();
	}
	
	//The setter/getter methods for peer object's properties
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

	public Double getDropRate() {
		return dropRate;
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
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

	public synchronized Integer getSequenceNum1() {
		return sequenceNum1;
	}

	public synchronized void setSequenceNum1(Integer sequenceNum1) {
		this.sequenceNum1 = sequenceNum1;
	}
	
	public synchronized void incrementSequenceNum1() {
		this.sequenceNum1++;
	}
	
	public synchronized Integer getSequenceNum2() {
		return sequenceNum2;
	}

	public synchronized void setSequenceNum2(Integer sequenceNum2) {
		this.sequenceNum2 = sequenceNum2;
	}
	
	public synchronized void incrementSequenceNum2() {
		this.sequenceNum2++;
	}

	public DatagramSocket getUdpFileRecSocket() {
		return udpFileRecSocket;
	}

	public void setUdpFileRecSocket(DatagramSocket udpFileRecSocket) {
		this.udpFileRecSocket = udpFileRecSocket;
	}

	public synchronized Boolean getRunning() {
		return running;
	}

	public synchronized void setRunning(Boolean running) {
		this.running = running;
	}

	public Long getStartTime() {
		return startTime;
	}
}
