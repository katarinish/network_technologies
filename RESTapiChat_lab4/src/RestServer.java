import com.sun.net.httpserver.*;

import java.io.*;
import java.util.*;
import java.net.*;

import org.json.*;

public class RestServer {
    private InetSocketAddress socketAddress;
    private HttpServer server;

    // Key<login> Value<id>
    private Map<String, Integer> chatUsernames;

    // Key<id> Value<User>
    private Map<Integer, User> chatUsers;

    // Key<AuthToken> Value<Time>
    private Map<String, Long> lastUserActivity;



    public RestServer(int usersCapacity) throws IOException {
        socketAddress = new InetSocketAddress(InetAddress.getByName(null), 8888);
        server = HttpServer.create(socketAddress, 0);

        chatUsernames = new HashMap<>(usersCapacity);
        chatUsers = new HashMap<>(usersCapacity);
        lastUserActivity = new HashMap<>(usersCapacity);
    }

    //TODO: дополнительный поток который проверяет активность юзеров
    public void start() {
        bindContext();
        server.start();

        System.out.println("**********");
        System.out.println("Server is currently running at " + socketAddress);
        System.out.println("**********");
    }

    private void bindContext() {
        server.createContext("/login", new LoginHandler(chatUsernames,
                chatUsers,
                lastUserActivity));

        server.setExecutor(null);
    }
}
