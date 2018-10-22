import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private String uuid;
    private String senderName;

    Message(String message, String uuid, String senderName) {
        this.message = message;
        this.uuid = uuid;
        this.senderName = senderName;
    }

    @Override
    public String toString(){
        return senderName + ": " + message;
    }
}
