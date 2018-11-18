import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            int userId = parseUserId(exchange);
            if (userId < 0) {
                RestServer.sendResponse(StatusCode.OK, activeUsersList(),
                        exchange);

                return;
            }

            if(!validateUserId(userId)) {
                RestServer.sendResponse(StatusCode.BAD_REQUEST, new JSONObject(),
                        exchange);

                return;
            }

            RestServer.sendResponse(StatusCode.OK, getUserInfo(userId),
                    exchange);
        } finally {
            exchange.close();

        }
    }

    private int parseUserId(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getRawPath();

        String regexp = "\\d+$";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(requestPath);

        return matcher.find() ? Integer.parseInt(matcher.group()) : -1 ;
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

    private boolean validateUserId(int userId) {
        return chatUsers.containsKey(userId);
    }

    private JSONObject getUserInfo(int userId) {
        return chatUsers.get(userId).getInfo();
    }
}
