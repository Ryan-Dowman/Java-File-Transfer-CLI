import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    Socket socket;

    DataOutputStream fileOut;
    DataInputStream fileIn;

    ObjectOutputStream objectOut;
    ObjectInputStream objectIn;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        startConnection();
    }

    @Override
    public void run() {
        listen();
    }

    private void startConnection() {
        try {

            // Here we create the streams such that we can handle incoming/outgoing data stream (for file downloading) and incoming/outgoing object stream for passing data
            fileOut = new DataOutputStream(socket.getOutputStream());
            fileIn = new DataInputStream(socket.getInputStream());

            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());

            // Next we need to run this client handler on a new thread so we don't block the programs ability to handle other requests whilst listening for incoming data
            new Thread(this).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        
        // Here we listen for any incoming message with our object input stream
        try {
            while(true){
                System.out.println("Listening for client...");

                // All requests will be mapped onto the request object such that we know what the request is for (more info or file download)
                Request request = (Request) objectIn.readObject();

                if(request.type == RequestType.OBJECT){
                    System.out.println("Fetching client directory data...");
                    
                    // Host must start listening for the corresponsing chain of inputs from client to compile the information they want
                    System.out.println("Recieved: " + objectIn.readUTF());
                }

                else if(request.type == RequestType.FILE){
                    System.out.println("Fetching client file data...");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendObject() {
        try{
            
            // We must write back a request with the same type to ensure the client can handle it properly
            objectOut.writeObject(new Request(RequestType.OBJECT));
            objectOut.writeObject("Test String");
            
            // We need to ensure that the data is not held onto and is instead immediately sent to the client
            objectOut.flush();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
