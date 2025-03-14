import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


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
    boolean downloadInProgress = false;
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    boolean downloadNextFolder = false; // Stops console logging values

    public Client(String ipAddress) {
        createSocketConnections(ipAddress);
    }

    private void createSocketConnections(String ipAddress) {
        try {
            dataSocket = new Socket(ipAddress, Program.DATA_TARGET_PORT);
            dataOut = new DataOutputStream(dataSocket.getOutputStream());
            dataIn = new DataInputStream(dataSocket.getInputStream());
            
            objectSocket = new Socket(ipAddress, Program.OBJECT_TARGET_PORT);
            objectOut = new ObjectOutputStream(objectSocket.getOutputStream());
            objectIn = new ObjectInputStream(objectSocket.getInputStream());

            System.out.println("Client sockets sucessfully established");
            
            Runnable listenForData = this::listenForData;
            Runnable listenForObjects = this::listenForObjects;

            new Thread(listenForData).start();
            new Thread(listenForObjects).start();

            scheduledExecutorService.scheduleAtFixedRate(this::requestDownloads, 0, 1000, TimeUnit.MILLISECONDS);

        } catch (IOException e) {
            if((e instanceof EOFException) || (e.getMessage() != null && e.getMessage().contains("An established connection was aborted by the software in your host machine"))) {
                System.out.println("Host has already connected with another client!");
                System.out.println("Waiting 3s to try reconnect");
                try {
                    ShutDown();
                    Thread.sleep(3000);   
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                createSocketConnections(ipAddress);
            }else if(e.getMessage() != null || e.getMessage().contains("Connection refused")){
                System.out.println("Host does not exist!");
            }
            else e.printStackTrace();
        }
    }

    private void requestDownloads(){
        if(!fileDownloadPaths.isEmpty() && downloadInProgress == false){
            downloadInProgress = true;
            String filePath = fileDownloadPaths.poll();
            try {
                dataOut.writeUTF(filePath);
                dataOut.flush();
            } catch (IOException e) {
                if(e.getMessage() != null && e.getMessage().contains("Socket closed"));
                else e.printStackTrace();
            }
        }   
    }

    private void listenForData() {
        while(!dataSocket.isClosed()){
            try {
            
                String fileName = dataIn.readUTF();
                long fileSize = dataIn.readLong();
    
                byte[] buffer = new byte[Program.BUFFER_SIZE];
                int bytesRead = 0;
                long totalBytesRead = 0;
    
                String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
                String filePath = desktopPath + File.separator + fileName;
    
                try(FileOutputStream fos = new FileOutputStream(filePath)){
                    while (totalBytesRead < fileSize && (bytesRead = dataIn.read(buffer)) != -1) { 
                        fos.write(buffer, 0, bytesRead);
                        fos.flush();
                        totalBytesRead += bytesRead;
                    }
                }
    
                System.out.println(fileName + " file downloaded to Desktop sucessfully!");
                downloadInProgress = false;
            } catch (IOException e) {
                if(e.getMessage() != null && e.getMessage().contains("Socket closed")) {
                    System.out.println("Data socket disconnected");
                }else if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
                    System.out.println("Host connection has been lost");
                    ShutDown();
                }
                else e.printStackTrace();
            }
        }
    }

    private void listenForObjects() {
        while(!objectSocket.isClosed()) { 
            try {
                FilesAndDirectories filesAndDirectories = (FilesAndDirectories) objectIn.readObject();
    
                List<String> filePaths = filesAndDirectories.files;
                List<String> directoryPaths = filesAndDirectories.directories;
                
                if(downloadNextFolder){
                    for(String filePath : filePaths){
                        requestFileDownload(filePath, true);
                    }
                    downloadNextFolder = false;
                    continue;
                }

                PopulateShortHandPathMap(filePaths, currentFilePathsMap);
                PopulateShortHandPathMap(directoryPaths, currentDirectoryPathsMap);

                for(String filePath : currentFilePathsMap.keySet()){
                    System.out.println(filePath);
                }
    
                for(String directoryPath : currentDirectoryPathsMap.keySet()){
                    System.out.println(directoryPath);
                }

            } catch (IOException | ClassNotFoundException e) {
                if(e.getMessage() != null && e.getMessage().contains("Socket closed")) {
                    System.out.println("Object socket disconnected");
                }else if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
                    System.out.println("Host connection has been lost");
                    ShutDown();
                }
                else e.printStackTrace();
            }    
        }
    }

    public void requestFileDownload(String path) {
        requestFileDownload(path, false);
    }

    // Force download is for downloading all files in folders as the filePathMap and folderPathMap will not be updated
    public void requestFileDownload(String path, boolean forceDownload) {
        if (forceDownload) {
            System.out.println(path + " added to download request");
            fileDownloadPaths.add(path);
        }else if(currentFilePathsMap.get(path) != null){
            System.out.println(path + " added to download request");
            fileDownloadPaths.add(currentFilePathsMap.get(path));
        }else if(currentDirectoryPathsMap.get(path) != null){
            System.out.println("Requesting directory direct children files");
            downloadNextFolder = true;
            sendObject(currentDirectoryPathsMap.get(path), true);
        }else{
            System.out.println("Not a valid file!");
        }
    }
    
    public void sendObject(String path) {
        sendObject(path, false);
    }

    public void sendObject(String path, boolean isFullPath) {
        try {
            String fullPath;
            if(isFullPath){
                fullPath = path;
            }else if(path.equals("..")){
                fullPath = returnParentFolder();
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
    
    private String returnParentFolder(){
        String parentPath = "";
        String firstPath = currentDirectoryPathsMap.values().stream().findFirst().orElse(null);
        
        if(firstPath == null) firstPath = currentFilePathsMap.values().stream().findFirst().orElse(null);
        if(firstPath == null) return null;

        String[] pathParts = firstPath.split("\\\\");
        
        for(int index = 0; index < pathParts.length - 2; index++){
            parentPath += pathParts[index] + "\\";
        }

        return parentPath;
    }

    private void PopulateShortHandPathMap(List<String> paths, Map<String, String> pathsMap) {
        pathsMap.clear();
        if(paths.isEmpty()) return;
        
        for(String path : paths){
            int slashIndex = path.lastIndexOf("\\") == -1 ? 0 : path.lastIndexOf("\\") + 1;
            String shortHandPath = slashIndex < path.length() - 1 ? path.substring(slashIndex, path.length()) : path;
            pathsMap.put(shortHandPath, path);
        }
    }
    
    @Override
    void ShutDown() {
        try {
            if(this.dataOut != null) this.dataOut.flush();
            if(dataSocket != null && !dataSocket.isClosed()) this.dataSocket.close();
            
            if(this.objectOut != null) this.objectOut.flush();
            if(objectSocket != null && !objectSocket.isClosed()) this.objectSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
