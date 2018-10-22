import java.util.*;
import java.net.*;

public class Node {
    private String nodeName;

    private NodeInfo parent = null;
    private ArrayList<NodeInfo> children;

    private Receiver receiver;
    private Sender sender;

    private int port;
    private InetAddress ipAddress;

    private int lostPercentage;

    Node(String nodeName, int lostPercentage, int port) throws UnknownHostException {
        this.nodeName = nodeName;

        this.lostPercentage = lostPercentage;

        this.port = port;
        this.ipAddress = InetAddress.getLocalHost();
    }

    Node(String nodeName, int lostPercentage, int port, String parentIp, int parentPort) throws UnknownHostException {
        this(nodeName, lostPercentage, port);

        parent = new NodeInfo(parentPort, parentIp);
    }

    void startMessaging(){
        receiver = new Receiver();
        sender = new Sender();
    };
}
