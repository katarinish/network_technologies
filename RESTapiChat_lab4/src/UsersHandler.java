import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    private Map<String, String> authorizedUsers;
    private Map<Integer, User> chatUsers;

    public UsersHandler(Map<String, String> authorizedUsers,
                        Map<Integer, User> chatUsers) {
        this.chatUsers = chatUsers;
        this.authorizedUsers = authorizedUsers;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                RestServer.sendResponse(StatusCode.METHOD_NOT_ALLOWED,
                        new JSONObject(), exchange);

                return;
            }

            String authToken = RestServer.validateAuthToken(exchange, authorizedUsers);
            if (authToken == null) return;

            RestServer.sendResponse(StatusCode.OK, activeUsersList(),
                    exchange);

        } finally {
            exchange.close();

        }
    }

    private JSONObject activeUsersList() {
        JSONObject usersList = new JSONObject();
        User user;

        for(Map.Entry<Integer, User> e: chatUsers.entrySet()) {
            user = e.getValue();
            if (!user.isOnline()) continue;

            usersList.append("users", user.getInfo());
        }

        return usersList;
    }

}
