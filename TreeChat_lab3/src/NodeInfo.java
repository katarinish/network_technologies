import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeInfo {
    private int port;
    private InetAddress ipAddress;
    String id;

    NodeInfo(int port, InetAddress ipAddress) {
        this.port = port;
        this.ipAddress = ipAddress;
        this.id = computeId(this.ipAddress, this.port);
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

    public String getId() {
        return id;
    }

    private String computeId(InetAddress addr, int port){
        return addr.getHostName() + ':' + port;
    }
}
