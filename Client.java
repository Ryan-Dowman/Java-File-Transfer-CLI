
import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class Client extends User{

    Socket dataSocket;
    Socket objectSocket;

    DataInputStream dataIn;
    DataOutputStream dataOut;

    ObjectInputStream objectIn;
    ObjectOutputStream objectOut;

    Queue<String> fileDownloadPaths = new LinkedList<>();

    public Client() {
        createSocketConnections();
    }

    private void createSocketConnections() {
        try {
            // Data socket is expected first so must be created first
            dataSocket = new Socket("localhost", Program.TARGET_PORT);
            objectSocket = new Socket("localhost", Program.TARGET_PORT);

            dataIn = new DataInputStream(dataSocket.getInputStream());
            dataOut = new DataOutputStream(dataSocket.getOutputStream());

            objectIn = new ObjectInputStream(dataSocket.getInputStream());
            objectOut = new ObjectOutputStream(dataSocket.getOutputStream());

            System.out.println("Client sockets sucessfully established");
            
            Runnable listenForData = () -> listenForData();
            Runnable listenForObjects = () -> listenForObjects();

            // Place each socket listening on different threads to ensure that the processes do not halt one another
            new Thread(listenForData).start();
            new Thread(listenForObjects).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForData() {
        while(true){
            try {
            
                // First thing that will come in is the file name
                String fileName = dataIn.readUTF();
    
                // Second thing will be the file size
                long fileSize = dataIn.readLong();
    
                // Intialise a buffer to handle the insertion of data
                byte[] buffer = new byte[Program.BUFFER_SIZE];
    
                // Track how many total bytes have been read in each iteration of collecting file data
                int bytesRead = 0;
    
                // Track how many total bytes have been read so we know when the file is done
                long totalBytesRead = 0;
    
                // Get the path of the current user's desktop (compatible for multiple OS's)
                String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
    
                String filePath = desktopPath + File.separator + fileName;
    
                // Create the file output stream in a try block to ensure correct closure of resources
                try(FileOutputStream fos = new FileOutputStream(filePath)){
                    while (totalBytesRead < fileSize && (bytesRead = dataIn.read(buffer)) != -1) { 
                        fos.write(buffer, 0, bytesRead);
                        fos.flush();
    
                        totalBytesRead += bytesRead;
                    }
                }
    
                System.out.println(fileName + " file downloaded to Desktop sucessfully!");
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listenForObjects() {
        while(true){
            try {
                FilesAndDirectories filesAndDirectories = (FilesAndDirectories) objectIn.readObject();

                List<String> filePaths = filesAndDirectories.files;
                List<String> directoryPaths = filesAndDirectories.directories;

                for(String filePath : filePaths){
                    System.out.println(filePath);
                }

                for(String directoryPath : directoryPaths){
                    System.out.println(directoryPath);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendObject() {
        
    }    
}
