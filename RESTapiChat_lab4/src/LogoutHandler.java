import com.sun.net.httpserver.*;
import org.json.JSONObject;

import java.io.*;
import java.util.Map;

public class LogoutHandler implements HttpHandler{
    private Map<String, String> authorizedUsers;
    private Map<String, Integer> chatUsernames;
    private Map<Integer, User> chatUsers;
    private Map<String, Long> lastUserActivity;


    public LogoutHandler(Map<String, String> authorizedUsers,
                         Map<String, Integer> chatUsernames,
                         Map<Integer,User> chatUsers,
                         Map<String, Long> lastUserActivity) {
        this.authorizedUsers = authorizedUsers;
        this.chatUsernames = chatUsernames;
        this.chatUsers = chatUsers;
        this.lastUserActivity = lastUserActivity;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("POST")) {
                RestServer.sendResponse(StatusCode.METHOD_NOT_ALLOWED,
                        new JSONObject(), exchange);

                return;
            }

            String authToken = RestServer.validateAuthToken(exchange, authorizedUsers);
            if (authToken == null) return;

            removeUser(authToken);
            RestServer.sendResponse(StatusCode.OK, createGoodByeMsg(),
                    exchange);
        } finally {
            exchange.close();
        }

    }

    private void removeUser(String authToken) {
        String usernameToDelete = authorizedUsers.get(authToken);
        int userIdToDelete = chatUsernames.get(usernameToDelete);
        User userToDelete = chatUsers.get(userIdToDelete);

        authorizedUsers.remove(authToken);
        chatUsernames.remove(usernameToDelete);
        lastUserActivity.remove(authToken);
        userToDelete.setOnline(false);
    }

    private JSONObject createGoodByeMsg() {
        return new JSONObject().put("message", "Good bye!");
    }
}
