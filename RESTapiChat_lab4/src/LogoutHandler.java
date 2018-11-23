import com.sun.net.httpserver.*;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class LogoutHandler implements HttpHandler{
    private Map<String, String> authorizedUsers;
    private Map<String, Integer> chatUsernames;
    private Map<Integer, User> chatUsers;
    private Map<String, Long> lastUserActivity;
    private ArrayList<Message> messages;



    public LogoutHandler(Map<String, String> authorizedUsers,
                         Map<String, Integer> chatUsernames,
                         Map<Integer,User> chatUsers,
                         Map<String, Long> lastUserActivity,
                         ArrayList<Message> messages) {
        this.authorizedUsers = authorizedUsers;
        this.chatUsernames = chatUsernames;
        this.chatUsers = chatUsers;
        this.lastUserActivity = lastUserActivity;
        this.messages = messages;

    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (exchange.getRequestMethod().equals("OPTIONS")){
                RestServer.sendResponse(StatusCode.OK,
                        new JSONObject(), exchange);

                return;
            }

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
        String text = "User: " + usernameToDelete + " quit the chat! Bye bye!";

        authorizedUsers.remove(authToken);
        chatUsernames.remove(usernameToDelete);
        lastUserActivity.remove(authToken);
        userToDelete.setOnline(false);


        messages.add(new Message(messages.size(), text, -1, "server"));
    }

    private JSONObject createGoodByeMsg() {
        return new JSONObject().put("message", "Good bye!");
    }
}
