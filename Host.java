
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Host implements Runnable {

    ServerSocket server;
    ClientHandler clientHandler;

    public Host() {
        startHosting();
    }

    @Override
    public void run() {
        awaitClientConnection();
    }

    private void awaitClientConnection() {
        try{
            server = new ServerSocket(Program.TARGET_PORT);

            Socket clientConnection = server.accept();
            ClientHandler client = new ClientHandler(clientConnection);

            clientHandler = client;

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void startHosting() {
        new Thread(this).start();
    }
}
