import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Receiver extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;
    private DatagramPacket messagePacket;

    private HashMap<String, Long> acceptedMessages;
    private ConcurrentHashMap<String, DatagramPacket> sentMessages;
    private ConcurrentHashMap<String, Long> messagesToBeConfirmed;
    private LinkedBlockingQueue<Message> messagesToSend;

    private ConcurrentHashMap<String, NodeInfo> neighbors;

    private Random randomizer;
    private int lostPercentage = 0;

    Receiver(DatagramSocket socket,
             int lostPercentage,
             LinkedBlockingQueue<Message> messagesToSend,
             ConcurrentHashMap<String, NodeInfo> neighbors,
             ConcurrentHashMap<String, Long> messagesToBeConfirmed,
             ConcurrentHashMap<String, DatagramPacket> sentMessages) {
        this.socket = socket;
        this.messagePacket = new DatagramPacket(new byte[MessageInfo.BUFF_SIZE], MessageInfo.BUFF_SIZE);

        this.acceptedMessages = new HashMap<>(10000);
        this.sentMessages = sentMessages;
        this.messagesToBeConfirmed = messagesToBeConfirmed;
        this.messagesToSend = messagesToSend;

        this.neighbors = neighbors;

        this.lostPercentage = lostPercentage;
        this.randomizer = new Random();
    }

    @Override
    public void run() {
        startReceivingMessages();
    }

    private void startReceivingMessages() {
        try {
            while (!this.isInterrupted()) {
                socket.receive(messagePacket);
                onReceivingMessage(messagePacket);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            onException(socket, e);
        } finally {
            onDisconnect(socket);
        }
    }

    private void onReceivingMessage(DatagramPacket messagePacket) throws IOException, ClassNotFoundException, InterruptedException {
        if (isDroppingMessage()) return;

        byte[] receivedData;
        Message incomingMessage;
        MessageType messageType;
        int cameFromPort;
        InetAddress cameFromAddress;

        receivedData  = messagePacket.getData();
        incomingMessage = (Message)BytesUtil.toObject(receivedData);
        messageType = incomingMessage.getMsgType();
        cameFromPort = messagePacket.getPort();
        cameFromAddress = messagePacket.getAddress();

        switch (messageType) {
            case TEXT_MESSAGE:
                parseAsRegularMessage(incomingMessage, cameFromPort, cameFromAddress);
                break;
            case CONFIRMATION:
                parseAsConfirmationMessage(incomingMessage, messagePacket);
                break;
            case I_AM_YOUR_SON:
                parseAsJoiningMessage(incomingMessage, messagePacket,cameFromPort, cameFromAddress);
                break;
        }
    }

    private void parseAsJoiningMessage(Message incomingMessage, DatagramPacket messagePacket,
                 int cameFromPort, InetAddress cameFromAddress) throws IOException {
        String msgUuid = incomingMessage.getUuid();

        if(!acceptedMessages.containsKey(msgUuid)) {
            System.out.println("********************");
            System.out.println("New child: " + incomingMessage.getSenderName()
                    + " was joined to the tree.");
            System.out.println("********************");

            NodeInfo newChild = new NodeInfo(cameFromPort, cameFromAddress);
            neighbors.put(newChild.getId(), newChild);
        }

        acceptedMessages.put(msgUuid, System.currentTimeMillis());
        sendConfirmation(incomingMessage, cameFromPort, cameFromAddress);
    }

    private void parseAsConfirmationMessage(Message incomingMessage, DatagramPacket messagePacket) {
        String uniqueMsgId = Message.generateMessageId(incomingMessage, messagePacket);
        messagesToBeConfirmed.remove(uniqueMsgId);
    }

    private void parseAsRegularMessage(Message incomingMessage, int senderPort, InetAddress senderAddress) throws IOException, InterruptedException {
        String msgUuid = incomingMessage.getUuid();

        if(!acceptedMessages.containsKey(msgUuid)) {
            Message.printMessage(incomingMessage);
        }

        acceptedMessages.put(msgUuid, System.currentTimeMillis());

        sendConfirmation(incomingMessage, senderPort, senderAddress);
        messagesToSend.put(incomingMessage);
    }

    private boolean isDroppingMessage() {
        return (randomizer.nextInt(100) < lostPercentage);
    }

    private void sendConfirmation(Message incomingMessage, int port, InetAddress ipAddress) throws IOException {
        DatagramPacket packet = Message.buildUtilPacket(incomingMessage, MessageType.CONFIRMATION, port, ipAddress);
        socket.send(packet);
    }

    @Override
    public void onDisconnect(DatagramSocket socket) {
        if(socket != null) {
            System.out.println("Disconnecting... Closing socket...");
            socket.close();
        }
    }

    @Override
    public void onException(DatagramSocket s, Exception e) {
        System.out.println("Some problems occurred while receiving packets...");
        onDisconnect(s);
        e.printStackTrace();
    }
}
