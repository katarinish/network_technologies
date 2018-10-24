import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryHandler extends Thread {
    private DatagramSocket socket = null;

    private HashMap<String, Long> acceptedMessages;
    private ConcurrentHashMap<String, Long> messagesToBeConfirmed;
    ConcurrentHashMap<String, DatagramPacket> sentMessages;

    private ConcurrentHashMap<String, NodeInfo> neighbors;

    DeliveryHandler(ConcurrentHashMap<String, DatagramPacket> sentMessages,
                    HashMap<String, Long> acceptedMessages,
                    ConcurrentHashMap<String, Long> messagesToBeConfirmed,
                    ConcurrentHashMap<String, NodeInfo> neighbors,
                    DatagramSocket socket) {
        this.socket = socket;
        this.acceptedMessages = acceptedMessages;
        this.messagesToBeConfirmed = messagesToBeConfirmed;
        this.sentMessages = sentMessages;
        this.neighbors = neighbors;
    }

    @Override
    public void run() {
        try {
            startHandlingMessages();
        } catch (IOException | InterruptedException e) {
            onException(socket, e);
        }
    }

    private void startHandlingMessages() throws IOException, InterruptedException {
       while (!this.isInterrupted()) {
           for (Map.Entry<String, Long> e : messagesToBeConfirmed.entrySet()) {
               Long timeDiff = System.currentTimeMillis() - e.getValue();

               if (timeDiff > Settings.ALIVE_TIMEOUT) {
                   messagesToBeConfirmed.entrySet().remove(e);
               } else {
                   DatagramPacket p = sentMessages.get(e.getKey());
                   socket.send(p);
               }
           }
       }

       sleep(500);
    }

    private void onDisconnect(DatagramSocket socket) {
        if(socket != null) {
            System.out.println("Disconnecting... Closing socket...");
            socket.close();
        }
    }

    private void onException(DatagramSocket s, Exception e) {
        System.out.println("Some problems occurred while receiving packets...");
        onDisconnect(s);
        e.printStackTrace();
    }


}
