import java.util.*;
import java.net.*;
import java.io.*;

public interface ConnectionListener {
    void onDisconnect(DatagramSocket socket);
    void onException(DatagramSocket s, Exception e);
}
