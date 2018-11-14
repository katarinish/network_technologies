import com.sun.net.httpserver.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    private Map<String, Integer> chatUsernames;
    private Map<Integer, User> chatUsers;
    private Map<String, Long> lastUserActivity;

    private int connectedUsersCount = 0;
    private User newCurrentUser;


    public LoginHandler(Map<String, Integer> chatUsernames,
                        Map<Integer, User> chatUsers,
                        Map<String, Long> lastUserActivity) {
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

            JSONObject reqData = RestServer.getRequestData(exchange);
            if (!validateLogin(reqData, exchange)) {
                return;
            }

            RestServer.sendResponse(StatusCode.OK, newCurrentUser.toJson(), exchange);
        } finally {
            exchange.close();
        }
    }

    private boolean validateLogin(JSONObject reqData, HttpExchange exchange)
            throws IOException {
        try {
            String userLogin = reqData.getString("username");
            if (chatUsernames.containsKey(userLogin)) {
                exchange.getResponseHeaders().add("WWW-Authenticate",
                        "Token realm='Username is already in use'");
                RestServer.sendResponse(StatusCode.UNAUTHORIZED,
                        new JSONObject(), exchange);

                return false;
            }

            newCurrentUser = new User(userLogin, ++connectedUsersCount);
            chatUsernames.put(userLogin, connectedUsersCount);
            chatUsers.put(connectedUsersCount, newCurrentUser);
            lastUserActivity.put(newCurrentUser.getAuthToken(), System.currentTimeMillis());

            System.out.println("New user is connected to the chat: " + userLogin);
        } catch (JSONException e) {
            RestServer.sendResponse(StatusCode.BAD_REQUEST, new JSONObject(), exchange);

            return false;
        }

        return true;
    }

}
