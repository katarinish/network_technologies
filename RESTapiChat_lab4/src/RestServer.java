import com.sun.net.httpserver.*;

import java.io.*;
import java.util.*;
import java.net.*;

import org.json.*;

public class RestServer {
    private InetSocketAddress socketAddress;
    private HttpServer server;

    // Key<auth_token> Value<login>
    private Map<String, String> authorizedUsers;

    // Key<login> Value<id>
    private Map<String, Integer> chatUsernames;

    // Key<id> Value<User>
    private Map<Integer, User> chatUsers;

    // Key<AuthToken> Value<Time>
    private Map<String, Long> lastUserActivity;

    //All messages sent from clients
    private ArrayList<Message> messages;

    public static JSONObject getRequestData(HttpExchange exchange) throws JSONException {
        InputStream inputStream = exchange.getRequestBody();

        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String jsonString = s.hasNext() ? s.next() : "";

        return new JSONObject(jsonString);
    }

    public static void sendResponse(StatusCode statusCode,
                                    JSONObject responseData,
                                    HttpExchange exchange) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        Headers headers = exchange.getResponseHeaders();
        byte[] dataInBytes = responseData.toString().getBytes();

        headers.add("Content-Type", "application/json");
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.add("Access-Control-Allow-Methods", "POST, GET");
        exchange.sendResponseHeaders(statusCode.getCode(), dataInBytes.length);
        outputStream.write(dataInBytes);
    }

    public static String validateAuthToken(HttpExchange exchange, Map authorizedUsers)
            throws IOException {
        List authHeader = exchange.getRequestHeaders().get("Authorization");
        if (authHeader == null) {
            RestServer.sendResponse(StatusCode.UNAUTHORIZED, new JSONObject(),
                    exchange);

            return null;
        }

        String tokenToValidate = (String)authHeader.get(0);
        if (!authorizedUsers.containsKey(tokenToValidate)) {
            RestServer.sendResponse(StatusCode.FORBIDDEN, new JSONObject(),
                    exchange);

            return null;
        }

        return tokenToValidate;
    }

    public RestServer(int usersCapacity) throws IOException {
        socketAddress = new InetSocketAddress(InetAddress.getByName(null), 8888);
        server = HttpServer.create(socketAddress, 0);

        authorizedUsers = new HashMap<>(usersCapacity);
        chatUsernames = new HashMap<>(usersCapacity);
        chatUsers = new HashMap<>(usersCapacity);
        lastUserActivity = new HashMap<>(usersCapacity);
        messages = new ArrayList<>(usersCapacity * 100);
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
        server.createContext("/login", new LoginHandler(authorizedUsers,
                chatUsernames,
                chatUsers,
                lastUserActivity));
        server.createContext("/logout", new LogoutHandler(authorizedUsers,
                chatUsernames,
                chatUsers,
                lastUserActivity));
        server.createContext("/users", new UsersHandler(authorizedUsers,
                chatUsers));
        server.createContext("/messages", new MessagesHandler(authorizedUsers,
                chatUsernames,
                chatUsers,
                lastUserActivity,
                messages));

        server.setExecutor(null);
    }
}
