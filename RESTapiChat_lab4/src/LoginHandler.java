import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    private InputStream inputStream;
    private OutputStream outputStream;

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
                //TODO: кинуть 405 ошибку
                return;
            }

            JSONObject reqData = RestServer.getRequestData(exchange);

            if (!validateLogin(reqData, exchange)) {
                //TODO: HTTP 401 header WWW-Authenticate value Token realm='Username is already in use'.
                return;
            }

            RestServer.sendResponse(200, newCurrentUser.toJson(), exchange);
        } finally {
            exchange.close();
        }


    }

    private boolean validateLogin(JSONObject reqData, HttpExchange exchange) {
        try {
            String userLogin = reqData.getString("username");
            if (chatUsernames.containsKey(userLogin)) {
                //TODO: HTTP 401 header WWW-Authenticate value Token realm='Username is already in use'.
                return false;
            }

            newCurrentUser = new User(userLogin, ++connectedUsersCount);
            chatUsernames.put(userLogin, connectedUsersCount);
            chatUsers.put(connectedUsersCount, newCurrentUser);
            lastUserActivity.put(newCurrentUser.getAuthToken(), System.currentTimeMillis());

        } catch (JSONException e) {
            //TODO: кинуть 400 - запрос не соответствует форамту
            return false;
        }

        return true;
    }

}
