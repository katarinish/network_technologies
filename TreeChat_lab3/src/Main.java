import java.util.*;
import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost() + "********" + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
