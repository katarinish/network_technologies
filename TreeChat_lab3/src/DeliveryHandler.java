import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryHandler extends Thread {
    private DatagramSocket socket = null;

    private HashMap<String, Message> acceptedMessages;
    private ConcurrentHashMap<String, DatagramPacket> messagesToBeConfirmed;

    private ConcurrentHashMap<String, NodeInfo> neighbors;

}
