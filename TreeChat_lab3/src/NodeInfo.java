import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeInfo {
    private int port;
    private InetAddress ipAddress;

    NodeInfo(int port, InetAddress ipAddress) {
        this.port = port;
        this.ipAddress = ipAddress;
    }

    NodeInfo(int port, String ipAddress) throws UnknownHostException {
        this(port, InetAddress.getByName(ipAddress));
    }

    public int getPort() {
        return port;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }
}
