import java.util.*;
import java.net.*;
import java.io.*;

public class Receiver extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;
    private DatagramPacket messagePacket;

    private HashMap<String, Message> acceptedMessages;

    Receiver(DatagramSocket socket) {
        this.socket = socket;
        this.messagePacket = new DatagramPacket(new byte[MessageInfo.BUFF_SIZE], MessageInfo.BUFF_SIZE);
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
