import java.util.*;
import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        Message newMsg = new Message("Hello world", "1", "Katarina");
        try {
            byte[] data = BytesUtil.toByteArray(newMsg);
            Message unserialized = (Message)BytesUtil.toObject(data);


            System.out.println(data.length);
            System.out.println(unserialized);
        } catch (IOException e) {
            System.out.println("Something went wrong");
        } catch (ClassNotFoundException e) {
            System.out.println("Something went wrong!!");
        }

    }
}
