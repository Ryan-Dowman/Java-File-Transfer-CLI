import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements Runnable{
    Socket socket;

    DataOutputStream fileOut;
    DataInputStream fileIn;

    ObjectOutputStream objectOut;
    ObjectInputStream objectIn;

    Boolean fileStreamInProgress = false;

    public Client(Socket socket) {
        this.socket = socket;
        startConnection();
    }

    private void startConnection() {
        try {
            fileOut = new DataOutputStream(socket.getOutputStream());
            fileIn = new DataInputStream(socket.getInputStream());

            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());

            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        requestIntialDirectories();
        listen();
    }

    private void requestIntialDirectories() {
        requestDirectoriesFromPath("");
    }

    private void listen() {
        try {
            while(true){
                Request request = (Request) objectIn.readObject();
                
                // If request type is object read the next inbound input as UTF and display
                if(request.type == RequestType.OBJECT){
                    System.out.println(objectIn.readUTF());
                }
            }            
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    private void requestDirectoriesFromPath(String path) {
        try {
            
            // Here we ask for the path we are looking for by sending our request and then write the string to the host using writeUTF
            Request request = new Request(RequestType.OBJECT);
            
            objectOut.writeObject(request);
            objectOut.writeUTF(path);
            
            objectOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
