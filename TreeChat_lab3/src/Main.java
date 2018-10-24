import java.util.*;
import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        Message newMsg = new Message("Hello world", "1", "Katarina", MessageType.TEXT_MESSAGE);
        try {
            byte[] data = BytesUtil.toByteArray(newMsg);
            Message unserialized = (Message)BytesUtil.toObject(data);

            MessageType msgType = MessageType.CONFIRMATION;

            switch (msgType) {
                case TEXT_MESSAGE:
                    System.out.println("ЭТО ТЕКСТОВОЕ СООБЩЕНИЕ");
                    break;
                case CONFIRMATION:
                    System.out.println("ЭТО ПОДТВЕРЖДЕНИЕ СООБЩЕНИЯ");
                    break;
            }


            System.out.println(data.length);
            System.out.println(unserialized);
        } catch (IOException e) {
            System.out.println("Something went wrong");
        } catch (ClassNotFoundException e) {
            System.out.println("Something went wrong!!");
        }

    }
}
