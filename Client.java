import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import javax.swing.filechooser.FileSystemView;

public class Client implements Runnable{
    Socket socket;

    DataOutputStream fileOut;
    DataInputStream fileIn;

    ObjectOutputStream objectOut;
    ObjectInputStream objectIn;

    Boolean fileStreamInProgress = false;

    List<String> currentDirectories;
    List<String> currentFiles;

    Queue<String> downloadQueue;

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
                    handleIncomingDirectories();
                }else if(request.type == RequestType.FILE) {
                    handleIncomingFile();
                }
            }            
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    public void requestDirectoriesFromPath(String path) {
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

    private void handleIncomingDirectories() {
        try {
          
            // The next incoming value with an instance of the FilesAndDirectories class that will contain all the files and directories present
            FilesAndDirectories filesAndDirectories = (FilesAndDirectories) objectIn.readObject();

            currentDirectories = filesAndDirectories.directories;
            currentFiles = filesAndDirectories.files;

            // Print out all directories
            for(String directoryPath : currentDirectories){
                System.out.println(directoryPath);
            }

            // Print all files
            for(String filePath : currentFiles){
                System.out.println("\t"+filePath.substring(filePath.lastIndexOf("\\") == -1 ? 0 : filePath.lastIndexOf("\\") + 1 , filePath.length()));
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void requestFileFromPath(String path) {
        
        downloadQueue.add(path);
        
        try {
            fileStreamInProgress = true;
            Request request = new Request(RequestType.FILE);
            System.out.println("Downloading "+ path+"...");

            objectOut.writeObject(request);
            objectOut.writeUTF(path);

            objectOut.flush();

        } catch (IOException e) {
            fileStreamInProgress = false;
        }
    }

    public boolean confirmPathIsCurrentFile(String path){
        return currentFiles.stream().anyMatch(filePath -> path.endsWith(filePath.substring(filePath.lastIndexOf("\\") == -1 ? 0 : filePath.lastIndexOf("\\") + 1 , filePath.length())));
    }
    
    public boolean confirmPathIsCurrentDirectory(String path){
        return currentDirectories.contains(path.trim());
    }

    private void handleIncomingFile() {
        
        try {
            System.out.println("File Downloading!");
            String fileName  = fileIn.readUTF();
            long fileSize = fileIn.readLong();
            
            FileSystemView view = FileSystemView.getFileSystemView();
            File file = view.getHomeDirectory();
            String desktopPath = file.getPath();
            String filePath = desktopPath +"\\" + fileName;

            byte[] buffer = new byte[Program.BUFFER_SIZE];
            int bytesRead = 0;
            int totalBytesRead = 0;

            System.out.println("Downloading to: "+filePath);

            try(FileOutputStream fos = new FileOutputStream(filePath)){
                while (totalBytesRead < fileSize && (bytesRead = fileIn.read(buffer)) != -1) { 
                    fos.write(buffer, 0, bytesRead);
                    fos.flush();
                    totalBytesRead += bytesRead;
                }
            }

            System.out.println("File Download Done!");
            fileStreamInProgress = false;

        } catch (IOException e) {
            fileStreamInProgress = false;
        }


    }

    String getFullFilePath(String path) {
        return currentFiles.stream().filter(filePath -> filePath.endsWith(path)).findFirst().get();
    }

    String getFullDirectoryPath(String path) {
        return currentDirectories.stream().filter(directoryPath -> directoryPath.endsWith(path)).findFirst().get();
    }
}
