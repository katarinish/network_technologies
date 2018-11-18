import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class MessagesHandler implements HttpHandler {
    private Map<String, String> authorizedUsers;
    private Map<String, Integer> chatUsernames;
    private Map<Integer, User> chatUsers;
    private Map<String, Long> lastUserActivity;
    private ArrayList<Message> messages;

    private final int DEFAULT_OFFSET = 0;
    private final int DEFAULT_COUNT = 10;

    private Message currentMessage;

    public MessagesHandler(Map<String, String> authorizedUsers,
                           Map<String, Integer> chatUsernames,
                           Map<Integer, User> chatUsers,
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
            String authToken = RestServer.validateAuthToken(exchange, authorizedUsers);
            if (authToken == null) return;

            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    Map queryParams = parseQueryParameters(exchange);

                    break;

                case "POST":
                    addMessageToList(RestServer.getRequestData(exchange), authToken);
                    RestServer.sendResponse(StatusCode.OK, currentMessage.toJson(),
                            exchange);

                default:
                    RestServer.sendResponse(StatusCode.METHOD_NOT_ALLOWED,
                            new JSONObject(), exchange);
            }
        } catch (JSONException e) {
            RestServer.sendResponse(StatusCode.BAD_REQUEST, new JSONObject(),
                    exchange);
        } finally {
            exchange.close();
        }
    }

    private Map parseQueryParameters(HttpExchange exchange) {
        Map<String, String> queryParameters = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        System.out.println("reqURIQuery" + query);
        if (query == null || query.equals("")) return null;

        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                queryParameters.put(pair[0], pair[1]);
            } else {
                queryParameters.put(pair[0], "");
            }
        }

        return queryParameters;
    }

    private JSONObject messagesList(Map queryParameters) {
        return new JSONObject();
    }

    private void addMessageToList(JSONObject reqData, String authToken)
            throws JSONException {
        String incomingMsg = reqData.getString("message");
        int msgId = messages.size();

        String author = authorizedUsers.get(authToken);
        int authorId = chatUsernames.get(author);

        currentMessage = new Message(msgId, incomingMsg, authorId, author);

        messages.add(currentMessage);
    }
}
