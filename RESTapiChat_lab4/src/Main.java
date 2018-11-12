import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            RestServer server = new RestServer(100);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
