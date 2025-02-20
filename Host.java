import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Host extends User {

    ServerSocket dataServer;
    ServerSocket objectServer;

    Socket clientDataSocket;
    Socket clientObjectSocket;

    DataInputStream dataIn;
    DataOutputStream dataOut;

    ObjectInputStream objectIn;
    ObjectOutputStream objectOut;

    public Host(ServerSocket dataServer, ServerSocket objectServer) {
        this.dataServer = dataServer;
        this.objectServer = objectServer;

        // Make the host client connection run on another thread
        Runnable listenForClientDataConnections = this::listenForClientDataConnections;
        Runnable listenForClientObjectConnections = this::listenForClientObjectConnections;

        new Thread(listenForClientDataConnections).start();
        new Thread(listenForClientObjectConnections).start();
    }

    private void listenForClientDataConnections(){
        while (true) { 
            try {
                Socket clientDataConnection = dataServer.accept();
                if(clientDataSocket == null){
                    clientDataSocket = clientDataConnection;

                    dataOut = new DataOutputStream(clientDataSocket.getOutputStream());
                    dataIn = new DataInputStream(clientDataSocket.getInputStream());

                    System.out.println("Client data socket connected!");
                    if(clientObjectSocket != null) sendIntialDirectories();
                }else{
                    System.out.println("Client attempted to connect but was rejected as another client has already been established");
                    clientDataConnection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void listenForClientObjectConnections(){
        while (true) { 
            try {
                Socket clientObjectConnection = dataServer.accept();
                if(clientObjectSocket == null){
                    clientObjectSocket = clientObjectConnection;

                    dataOut = new DataOutputStream(clientObjectSocket.getOutputStream());
                    dataIn = new DataInputStream(clientObjectSocket.getInputStream());

                    System.out.println("Client object socket connected!");
                    if(clientDataSocket != null) sendIntialDirectories();
                }else{
                    System.out.println("Client attempted to connect but was rejected as another client has already been established");
                    clientObjectConnection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendObject(String path){
        try {
            String stringRequestedDirectoryPath = path;
            Path requestedDirectoryPath = Paths.get(stringRequestedDirectoryPath);
            
            File directory = requestedDirectoryPath.toFile();			
            File[] files = stringRequestedDirectoryPath.equals("") ? File.listRoots() : directory.listFiles();
            
            FilesAndDirectories filesAndDirectories = createFilesAndDirectoriesFromFiles(files);

            objectOut.writeObject(filesAndDirectories);
            objectOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendIntialDirectories(){
        System.out.println("Client sockets sucessfully connected!");
        sendObject("");
    }

    private FilesAndDirectories createFilesAndDirectoriesFromFiles(File[] files) {
        
        List<String> filePaths = new ArrayList<>();
        List<String> directoryPaths = new ArrayList<>();

        for(File file : files){
            if(file.isFile()) filePaths.add(file.getPath());
            else if(file.isDirectory()) directoryPaths.add(file.getPath());
        }
        
        FilesAndDirectories filesAndDirectories = new FilesAndDirectories(filePaths, directoryPaths);

        return filesAndDirectories;
    }
}
