import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Receiver extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;
    private DatagramPacket messagePacket;

    private HashMap<String, Message> acceptedMessages;
    private ConcurrentHashMap<String, DatagramPacket> messagesToBeConfirmed;

    Receiver(DatagramSocket socket,
             ConcurrentHashMap<String, DatagramPacket> messagesToBeConfirmed) {
        this.socket = socket;
        this.messagePacket = new DatagramPacket(new byte[MessageInfo.BUFF_SIZE], MessageInfo.BUFF_SIZE);
        this.acceptedMessages = new HashMap<>(1000);
        this.messagesToBeConfirmed = messagesToBeConfirmed;
    }

    @Override
    public void run() {
        startReceivingMessages();
    }

    void startReceivingMessages() {
        try {
            while (!this.isInterrupted()) {
                socket.receive(messagePacket);
                onReceivingMessage(messagePacket);
            }
        } catch (IOException e) {
            onException(socket, e);
        } finally {
            onDisconnect(socket);
        }

    }

    void onReceivingMessage(DatagramPacket messagePacket){
        //Берем порт
        int cameFromPort = messagePacket.getPort();
        //Берем адрес
        InetAddress cameFromAddress = messagePacket.getAddress();
        //Берем сообщение
        byte[] receivedData = messagePacket.getData();
        //Выводим сообщение
        //Отправляем подтверждение
        sendConfirmation(cameFromPort, cameFromAddress);
        //Рассылаем предкам и потомкам

    }

    void sendConfirmation(int port, InetAddress ipAddress) {

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
