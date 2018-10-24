import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;
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
    //String: UUID+InetAddress+Port  DatagramPacket: messagePacket
    private ConcurrentHashMap<String, DatagramPacket> sentMessages;
    //String: UUID+InetAddress+Port  Long: current time of sending msg
    private ConcurrentHashMap<String, Long> messagesToBeConfirmed;

    Node(String nodeName, int lostPercentage, int port) throws UnknownHostException, SocketException {
        this.nodeName = nodeName;

        this.neighbors = new ConcurrentHashMap<>(100);

        this.lostPercentage = lostPercentage;
        this.messagesToSend = new LinkedBlockingQueue<>(10000);
        this.messagesToBeConfirmed = new ConcurrentHashMap<>(10000);
        this.sentMessages = new ConcurrentHashMap<>(10000);

        this.port = port;
        this.ipAddress = InetAddress.getLocalHost();
        socket = new DatagramSocket(this.port, this.ipAddress);
        System.out.println("********************");
        System.out.println("New node was created!");
        System.out.println(nodeName + ": " + socket.getLocalAddress() + ':' + socket.getLocalPort());
        System.out.println("********************");
    }

    Node(String nodeName, int lostPercentage, int port, String parentIp, int parentPort) throws UnknownHostException, SocketException {
        this(nodeName, lostPercentage, port);

        parent = new NodeInfo(parentPort, parentIp);
        neighbors.put(parent.getId(), parent);
    }

    public void startMessaging(boolean joinParent){
        try {
            receiver = new Receiver(socket,lostPercentage, messagesToSend, neighbors, messagesToBeConfirmed, sentMessages);
            sender = new Sender(socket, messagesToSend, neighbors, messagesToBeConfirmed, sentMessages);

            receiver.start();
            sender.start();

            if(joinParent) { joinParent(); }

            startSendingMessages();
        } catch (InterruptedException | IOException e) {
            System.out.println("Thread was interrupted. Closing socket...");
            receiver.interrupt();
            sender.interrupt();
            if(socket != null) { socket.close(); }
        }
    }

    private void startSendingMessages() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        String enteredText;
        String uuid;
        Message msgToSend;

        while(!Thread.currentThread().isInterrupted()) {
            enteredText = scanner.nextLine();
            uuid = UUID.randomUUID().toString();

            msgToSend = new Message(enteredText, uuid, this.nodeName, MessageType.TEXT_MESSAGE);
            messagesToSend.put(msgToSend);
        }
    }
    private void joinParent() throws IOException {
        String uuid = UUID.randomUUID().toString();
        int receiverPort = parent.getPort();
        InetAddress receiverAddr = parent.getIpAddress();

        Message msgToSend = new Message("", uuid, this.nodeName, MessageType.I_AM_YOUR_SON);
        DatagramPacket packet = Message.buildUtilPacket(msgToSend,MessageType.I_AM_YOUR_SON,receiverPort,receiverAddr);
        socket.send(packet);

        String uniqueMsgId = Message.generateMessageId(msgToSend, packet);

        messagesToBeConfirmed.put(uniqueMsgId, System.currentTimeMillis());
        sentMessages.put(uniqueMsgId, packet);
    }
}
