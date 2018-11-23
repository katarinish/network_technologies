import org.json.JSONObject;

import java.util.UUID;

public class User {
    private String login;
    private int id;
    private String authToken;
    private Boolean isOnline;

    public User(String login, int id ) {
        this.login = login;
        this.id = id;

        authToken = UUID.randomUUID().toString();
        isOnline = true;
    }

    public String getLogin() {
        return login;
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getId() {
        return id;
    }

    public boolean isOnline() {
        return !(isOnline == null || !isOnline);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject
                .put("login", this.login)
                .put("id", this.id)
                .put("online", this.isOnline)
                .put("authToken", this.authToken);

        return jsonObject;

    }

    public JSONObject getInfo() {
        JSONObject fullInfo = this.toJson();
        fullInfo.remove("authToken");

        return fullInfo;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }
}
