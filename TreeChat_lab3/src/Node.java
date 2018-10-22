import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Node {
    private String nodeName;

    private NodeInfo parent = null;
    private ConcurrentHashMap<String, NodeInfo> neighbors;

    private Receiver receiver;
    private Sender sender;

    private int port;
    private InetAddress ipAddress;
    private DatagramSocket socket = null;

    private int lostPercentage;

    private LinkedBlockingQueue<Message> messagesToSend;
    private ConcurrentHashMap<String, Message> messagesToBeConfirmed;

    Node(String nodeName, int lostPercentage, int port) throws UnknownHostException, SocketException {
        this.nodeName = nodeName;

        this.neighbors = new ConcurrentHashMap<>(100);

        this.lostPercentage = lostPercentage;
        this.messagesToSend = new LinkedBlockingQueue<>(1000);
        this.messagesToBeConfirmed = new ConcurrentHashMap<>(1000);

        this.port = port;
        this.ipAddress = InetAddress.getLocalHost();
        socket = new DatagramSocket(this.port, this.ipAddress);
    }

    Node(String nodeName, int lostPercentage, int port, String parentIp, int parentPort) throws UnknownHostException, SocketException {
        this(nodeName, lostPercentage, port);

        parent = new NodeInfo(parentPort, parentIp);
        neighbors.put(parent.getId(), parent);
    }

    void startMessaging(){
        receiver = new Receiver(socket);
        receiver.start();
        sender = new Sender(socket);
        sender.start();
    };
}
