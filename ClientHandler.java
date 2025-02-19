import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable{
    Socket objectSocket;
    Socket fileSocket;

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
                    String path = objectIn.readUTF();
                    System.out.println("Recieved Path: " + path);

                    sendObject(path);
                }

                else if(request.type == RequestType.FILE){
                    // Host must start listening for the corresponsing chain of inputs from client to compile the information they want
                    String path = objectIn.readUTF();
                    System.out.println("Recieved Path: " + path);

                    sendFile(path);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendObject(String path) {
        try{

            Path requestedDirectoryPath = Paths.get(path);

            File directory = requestedDirectoryPath.toFile();			
			File[] directoriesAndFiles = path.equals("") ? File.listRoots() : directory.listFiles();

            FilesAndDirectories filesAndDirectories = createFilesAndDirectoriesFromUnsortedDirectories(directoriesAndFiles);
            
            // We must write back a request with the same type to ensure the client can handle it properly
            objectOut.writeObject(new Request(RequestType.OBJECT));  
            objectOut.writeObject(filesAndDirectories);
            
            // We need to ensure that the data is not held onto and is instead immediately sent to the client
            objectOut.flush();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private FilesAndDirectories createFilesAndDirectoriesFromUnsortedDirectories(File[] directoriesAndFiles) {
        List<String> filePaths = new ArrayList<>();
		List<String> directoryPaths = new ArrayList<>();
		
		if(directoriesAndFiles != null) {
			for(File file : directoriesAndFiles) {
				if(file.isFile()) filePaths.add(file.getAbsolutePath());
				if(file.isDirectory()) directoryPaths.add(file.getAbsolutePath());
			}
		}
		
		FilesAndDirectories filesAndDirectories = new FilesAndDirectories(filePaths, directoryPaths);
		return filesAndDirectories;
    }

    private void sendFile(String path) {
        try {
            System.out.println("Sending file "+path+" to client");
    
            File file = Paths.get(path).toFile();
    
            objectOut.writeObject(new Request(RequestType.FILE));
            objectOut.flush();

            fileOut.writeUTF(file.getName());
            fileOut.writeLong(file.length());

            byte[] buffer = new byte[Program.BUFFER_SIZE];
            int bytesRead = 0;

            try(FileInputStream fis = new FileInputStream(file)){
                while((bytesRead = fis.read(buffer)) != -1){
                    fileOut.write(buffer, 0, bytesRead);
                    fileOut.flush();
                }
            }

            System.out.println("File sent!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
