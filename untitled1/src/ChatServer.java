import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.*;

@ServerEndpoint("/ws")
public class ChatServer {
    private Map<String, String> usernames = new HashMap<String, String>();

    @OnOpen
    public void open(Session session) throws IOException, EncodeException {
        JSONObject msg = new JSONObject();
        msg.put("author", "Server");
        msg.put("message", "Welcome to the chat room. Please state your username to begin.");
        session.getBasicRemote().sendText(msg.toString());

    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        if (usernames.containsKey(userId)) {
            String username = usernames.get(userId);
            usernames.remove(userId);
            for (Session peer : session.getOpenSessions())
                peer.getBasicRemote().sendText("(Server): " + username + " left the chat room.");
        }
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException, EncodeException {
        String userId = session.getId();

        if (usernames.containsKey(userId)) {
            for (Session peer : session.getOpenSessions())
                peer.getBasicRemote().sendText(message);
        } else {
            if (usernames.containsValue(message) || message.toLowerCase().equals("server")) {
                JSONObject msg = new JSONObject();
                msg.put("author", "Server");
                msg.put("message", "Username is already in use");
                session.getBasicRemote().sendText(msg.toString());
            }
            else {
                usernames.put(userId, message);
                for (Session peer : session.getOpenSessions()){
                    peer.getBasicRemote().sendText(message);
                }
            }
        }
    }
}
