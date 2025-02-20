import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Host extends User implements Runnable {

    ServerSocket server;
    Socket clientDataSocket;
    Socket clientObjectSocket;

    public Host(ServerSocket server) {
        // Assign the server that is passed in
        this.server = server;

        // Make the host client connection run on another thread
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Hosting has begun");
        System.out.println("Awaiting client");

        listenForClientConnections();
    }

    private void listenForClientConnections() {
        while (true) { 
            if(clientDataSocket != null && clientObjectSocket != null){
                break;
            }
            try {
                Socket clientConnection = server.accept();
                if(clientDataSocket == null){
                    clientDataSocket = clientConnection;
                    System.out.println("Client data socket connected!");
                }else if (clientObjectSocket == null) {
                    clientObjectSocket = clientConnection;
                    System.out.println("Client object socket connected!");
                }else{
                    System.out.println("Client attempted to connect but was rejected as another client has already been established");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sendIntialDirectories();
    }
}
