import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;
    private DatagramPacket messagePacket = null;


    //String: UUID+InetAddress+Port  DatagramPacket: messagePacket
    private ConcurrentHashMap<String, DatagramPacket> sentMessages;
    //String: UUID+InetAddress+Port  Long: current time of sending msg
    private ConcurrentHashMap<String, Long> messagesToBeConfirmed;
    private LinkedBlockingQueue<Message> messagesToSend;

    private ConcurrentHashMap<String, NodeInfo> neighbors;

    Sender(DatagramSocket socket,
           LinkedBlockingQueue<Message> messagesToSend,
           ConcurrentHashMap<String, NodeInfo> neighbors,
           ConcurrentHashMap<String, Long> messagesToBeConfirmed,
           ConcurrentHashMap<String, DatagramPacket> sentMessages) {

        this.socket = socket;

        this.messagesToSend = messagesToSend;
        this.messagesToBeConfirmed = messagesToBeConfirmed;
        this.sentMessages = sentMessages;

        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        startSendingMessages();
    }

    private void startSendingMessages() {
        Message msgToSend;

        try {
            while (!this.isInterrupted()) {
                msgToSend = messagesToSend.take();
                sendToAllNeighbors(msgToSend);
            }
        } catch (InterruptedException e) {
            System.out.println("Sender thread was interrupted.");
            onException(socket, e);
        } finally {
            onDisconnect(socket);
        }
    }

    private void sendToAllNeighbors(Message msgToSend)  {
        neighbors.forEach((nodeId, nodeInfo) -> {
            try {
                sendToRecipient(nodeInfo, msgToSend);
            } catch (IOException e) {
                onException(socket, e);
            }
        });
    }

    private void sendToRecipient(NodeInfo recipientNode, Message msgToSend) throws IOException {
        int recipientPort = recipientNode.getPort();
        InetAddress recipientAddress = recipientNode.getIpAddress();

        DatagramPacket packetToSent = Message.buildMessagePacket(msgToSend, recipientPort, recipientAddress);
        socket.send(packetToSent);

        //uniqueMsgId: UUID+InetAddress+Port
        String uniqueMsgId = Message.generateMessageId(msgToSend, packetToSent);

        messagesToBeConfirmed.put(uniqueMsgId, System.currentTimeMillis());
        sentMessages.put(uniqueMsgId, packetToSent);
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
