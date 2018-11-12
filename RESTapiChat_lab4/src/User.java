import java.util.UUID;

public class User {
    private String login;
    private int id;
    private String authToken;
    private boolean isOnline;

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
        return isOnline;
    }
}
