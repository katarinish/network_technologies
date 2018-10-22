import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message implements Serializable {
    private String message;
    private String uuid;
    private String senderName;

    //+порт+адрес
    private String msgId;

    Message(String message, String uuid, String senderName) {
        this.message = message;
        this.uuid = uuid;
        this.senderName = senderName;
    }

    public static DatagramPacket buildMessagePacket(Message msg,
                                             int recipientPort,
                                             InetAddress recipientAddr) throws IOException {
        byte[] data = BytesUtil.toByteArray(msg);
        return new DatagramPacket(data, data.length, recipientAddr, recipientPort);
    }

    public String getUuid() {
        return uuid;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    @Override
    public String toString(){
        return senderName + ": " + message;
    }
}
