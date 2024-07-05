import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import static java.lang.System.exit;

public class ServerUpdateListener implements Runnable {
    private Kliens client;
    private Socket recvSocket;
    public ServerUpdateListener(Kliens client, Socket socket) throws IOException {
        this.client = client;
        this.recvSocket = socket;
    }
    @Override
    public void run() {
        try {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(recvSocket.getInputStream()));
            String ans;
            while (true) {
                ans = socketReader.readLine();
                if (!ans.startsWith("#")) {
                    client.addToChat(ans);
                } else if (ans.startsWith("#allUsers:")) {
                    client.refreshNames(ans);
                } else if (ans.startsWith("#errorName")) {
                    System.out.println("taken");
                    client.requestNewName();
                } else if (ans.startsWith("#activeUsers")) {
                    client.addToChat(ans.split("#")[2]);
                }
            }
        } catch (IOException e) {
            // ex. socket closed due to taken name
        }
    }
}
