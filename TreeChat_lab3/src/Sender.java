import java.net.*;

public class Sender extends Thread implements ConnectionListener {
    private DatagramSocket socket = null;

    private DatagramPacket messagePacket = null;


    Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

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
