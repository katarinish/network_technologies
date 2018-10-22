import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;
    private DatagramPacket messagePacket = null;

    private LinkedBlockingQueue<Message> messagesToSend;
    private ConcurrentHashMap<String, DatagramPacket> messagesToBeConfirmed;

    private ConcurrentHashMap<String, NodeInfo> neighbors;

    private int lostPercentage = 0;

    Sender(DatagramSocket socket,
           int lostPercentage,
           LinkedBlockingQueue<Message> messagesToSend,
           ConcurrentHashMap<String, NodeInfo> neighbors) {

        this.socket = socket;
        this.lostPercentage = lostPercentage;
        this.messagesToSend = messagesToSend;
        this.messagesToBeConfirmed = new ConcurrentHashMap<>(1000);
        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        startSendingMessages();
    }

    private  void startSendingMessages() {
        Message msgToSend;

        try {
            while (!this.isInterrupted()) {
                msgToSend = messagesToSend.take();
                sendToAllNeighbors(msgToSend);
            }
        } catch (IOException e) {
            onException(socket, e);
        } catch (InterruptedException e) {
            System.out.println("Sender thread was interrupted.");
            onDisconnect(socket);
        } finally {
            onDisconnect(socket);
        }
    }

    private void sendToAllNeighbors(Message msgToSend)  {
        neighbors.forEach((nodeName, nodeInfo) -> {
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
        String uuid = msgToSend.getUuid();

        DatagramPacket packetToSent = Message.buildMessagePacket(msgToSend, recipientPort, recipientAddress);
        socket.send(packetToSent);

        messagesToBeConfirmed.put(uuid, packetToSent);

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
