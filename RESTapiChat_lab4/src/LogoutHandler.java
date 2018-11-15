import com.sun.net.httpserver.*;
import org.json.JSONException;
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

            validateAuthToken(exchange);

        } finally {
            exchange.close();
        }

    }

    private boolean validateAuthToken(HttpExchange exchange) {
        Headers reqHeaders = exchange.getRequestHeaders();
        String tokenToValidate = reqHeaders.get("Authorization").get(0);

        System.out.println(tokenToValidate);
        return true;
    }
}
