import java.util.*;
import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Expected 3 obligated arguments: nodeName, lossPercentage, selfPort");
        System.out.println("Other 2 arguments are optional: parentPort, parentAddress");

        if (args.length < 3) {
            System.out.println("Not enough arguments");
            return;
        }

        Node node;

        String nodeName = args[0];
        int lossPercentage = Integer.valueOf(args[1]);
        int selfport = Integer.valueOf(args[2]);

        try {
            if (args.length == 5) {
                int parentPort = Integer.valueOf(args[3]);
                String parentAddr = args[4];

                node = new Node(nodeName, lossPercentage, selfport, parentAddr, parentPort);
                node.startMessaging(true);
            } else {
                node = new Node(nodeName, lossPercentage, selfport);
                node.startMessaging(false);
            }

        } catch (IOException e) {
            System.out.println("Something went wrong");
        }

    }
}
