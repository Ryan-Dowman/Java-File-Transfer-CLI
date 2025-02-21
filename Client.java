
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class Client extends User{

    Socket dataSocket;
    Socket objectSocket;

    DataInputStream dataIn;
    DataOutputStream dataOut;

    ObjectInputStream objectIn;
    ObjectOutputStream objectOut;

    Map<String, String> currentDirectoryPathsMap = new HashMap<>();
    Map<String, String> currentFilePathsMap = new HashMap<>();

    Queue<String> fileDownloadPaths = new LinkedList<>();

    public Client() {
        createSocketConnections();
    }

    private void createSocketConnections() {
        try {
            // Data socket is expected first so must be created first
            dataSocket = new Socket("localhost", Program.DATA_TARGET_PORT);
            dataOut = new DataOutputStream(dataSocket.getOutputStream());
            dataIn = new DataInputStream(dataSocket.getInputStream());
            
            objectSocket = new Socket("localhost", Program.OBJECT_TARGET_PORT);
            objectOut = new ObjectOutputStream(objectSocket.getOutputStream());
            objectIn = new ObjectInputStream(objectSocket.getInputStream());

            System.out.println("Client sockets sucessfully established");
            
            Runnable listenForData = this::listenForData;
            Runnable listenForObjects = this::listenForObjects;

            // Place each socket listening on different threads to ensure that the processes do not halt one another
            new Thread(listenForData).start();
            new Thread(listenForObjects).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForData() {
        System.out.println("Waiting for data...");
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
        while(true) { 
            try {
                FilesAndDirectories filesAndDirectories = (FilesAndDirectories) objectIn.readObject();
    
                List<String> filePaths = filesAndDirectories.files;
                List<String> directoryPaths = filesAndDirectories.directories;
    
                // We will use these to help navigate based on short hand versions of paths
                PopulateShortHandPathMap(filePaths, currentFilePathsMap);
                PopulateShortHandPathMap(directoryPaths, currentDirectoryPathsMap);

                for(String filePath : currentFilePathsMap.keySet()){
                    System.out.println(filePath);
                }
    
                for(String directoryPath : currentDirectoryPathsMap.keySet()){
                    System.out.println(directoryPath);
                }

                System.out.print("> ");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }    
        }
    }

    public void sendObject(String path) {
        try {

            String fullPath = null;

            // Really need to refactor this!
            if(path.equals("..")){
                String firstPath = currentDirectoryPathsMap.values().stream().findFirst().orElse(null);
                
                if(firstPath != null){
                    int slashIndex = firstPath.lastIndexOf("\\") == -1 ? 0 : firstPath.lastIndexOf("\\");
                    long numberOfSlashes = firstPath.chars().filter(ch -> ch == '\\').count();
                    if (numberOfSlashes != 1) {
                        fullPath = slashIndex != firstPath.length() - 1 ? firstPath.substring(0, slashIndex) : "";
                        fullPath = fullPath.equals("") ? "" : fullPath.substring(0, fullPath.lastIndexOf("\\") == -1 ? fullPath.length() : fullPath.lastIndexOf("\\"));
                    }else{
                        fullPath = "";
                    }
                }
            }else{
                fullPath = path.equals("") ? "" : currentDirectoryPathsMap.get(path);
            }

            if(fullPath == null) {
                System.out.println("Invalid Path!");
                return;
            }
            
            objectOut.writeUTF(fullPath.trim());
            objectOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    @Override
    void ShutDown() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void PopulateShortHandPathMap(List<String> paths, Map<String, String> pathsMap) {
        pathsMap.clear();
        
        for(String path : paths){
            int slashIndex = path.lastIndexOf("\\") == -1 ? 0 : path.lastIndexOf("\\") + 1;
            String shortHandPath = slashIndex < path.length() - 1 ? path.substring(slashIndex, path.length()) : path;
            pathsMap.put(shortHandPath, path);
        }
    }
}
