import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message implements Serializable {

    private String message;
    private String uuid;
    private String senderName;
    private MessageType msgType;

    //+порт+адрес
    private String msgId;

    Message(String message, String uuid, String senderName, MessageType msgType) {
        this.message = message;
        this.uuid = uuid;
        this.senderName = senderName;
        this.msgType = msgType;
    }

    public static DatagramPacket buildMessagePacket(Message msg,
                                             int recipientPort,
                                             InetAddress recipientAddr) throws IOException {
        byte[] data = BytesUtil.toByteArray(msg);
        return new DatagramPacket(data, data.length, recipientAddr, recipientPort);
    }

    public static DatagramPacket buildUtilPacket(Message msgToConfirm,
        MessageType msgType,
        int recipientPort, InetAddress recipientAddr) throws IOException {
        String uuid = msgToConfirm.getUuid();
        String senderName = msgToConfirm.getSenderName();

        Message confirmationMsg = new Message("", uuid, senderName, msgType);

        return Message.buildMessagePacket(confirmationMsg, recipientPort, recipientAddr);
    }

    public static String generateMessageId(Message msg, DatagramPacket packet) {
        return msg.getUuid() + packet.getSocketAddress();
    }

    public static void printMessage(Message msg) {
        System.out.println(msg);
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

    public String getMsgId() {
        return msgId;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    @Override
    public String toString(){
        return senderName + ": " + message;
    }
}
