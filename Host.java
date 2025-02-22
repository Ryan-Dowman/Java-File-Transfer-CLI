import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

        Runnable listenForClientDataConnections = this::listenForClientDataConnections;
        Runnable listenForClientObjectConnections = this::listenForClientObjectConnections;

        new Thread(listenForClientDataConnections).start();
        new Thread(listenForClientObjectConnections).start();

        System.out.println("Waiting for client...");
    }

    private void listenForClientDataConnections(){
        while (true) { 
            try {
                Socket clientDataConnection = dataServer.accept();
                if(clientDataSocket == null){
                    clientDataSocket = clientDataConnection;

                    dataOut = new DataOutputStream(clientDataSocket.getOutputStream());
                    dataIn = new DataInputStream(clientDataSocket.getInputStream());

                    Runnable handleIncomingData = this::handleIncomingData;
                    new Thread(handleIncomingData).start();

                    if(objectOut != null) sendIntialDirectories();
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
                Socket clientObjectConnection = objectServer.accept();
                if(clientObjectSocket == null){
                    clientObjectSocket = clientObjectConnection;

                    objectOut = new ObjectOutputStream(clientObjectSocket.getOutputStream());
                    objectIn = new ObjectInputStream(clientObjectSocket.getInputStream());

                    Runnable handleIncomingObject = this::handleIncomingObject;
                    new Thread(handleIncomingObject).start();

                    if(dataOut != null) sendIntialDirectories();
                }else{
                    System.out.println("Client attempted to connect but was rejected as another client has already been established");
                    clientObjectConnection.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendIntialDirectories(){
        System.out.println("Client sucessfully connected!");
        sendObject("");
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
            
            if(path.equals(""))System.out.println("Client inside root directory");
            else System.out.println("Client inside " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void handleIncomingData(){
        while (true) { 
            try {
                String filePath = dataIn.readUTF();
                sendFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFile(String path) {
		try {
			File file = Paths.get(path).toFile();
			
			dataOut.writeUTF(file.getName());
			dataOut.writeLong(file.length());
			
			byte[] buffer = new byte[Program.BUFFER_SIZE];
			int bytesRead = 0;
			
			try(FileInputStream fis = new FileInputStream(file)){
				while((bytesRead = fis.read(buffer)) != -1) {
					dataOut.write(buffer, 0, bytesRead);
					dataOut.flush();
				}
			}
			
			System.out.println("Client downloaded " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void handleIncomingObject(){
        while(true) { 
            try {
                String path = objectIn.readUTF();
                sendObject(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void bootClient(){
        clientDataSocket = null;
        clientObjectSocket = null;

        dataIn = null;
        dataOut = null;

        objectIn = null;
        objectOut = null;

        System.out.println("Current client booted");
    }

    
    @Override
    void ShutDown() {
        try {
            this.dataServer.close();
            this.objectServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
