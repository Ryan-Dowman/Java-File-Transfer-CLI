import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Program {
    
    public static int TARGET_PORT = 5000;
	public static final int BUFFER_SIZE = 32 * 1024; //32KB 

    public static void main(String[] args) {
        
        // Use try block to ensure Scanner resource is properly closes
        try(Scanner scanner = new Scanner(System.in)){
            
            String input = "";

            while(!input.equals("h") && !input.equals("c")){
                System.out.println("Host or Client?");
                input = scanner.nextLine();
            }

            if(input.equals("h")){
                System.out.println("Host selected");
                new Host();
            }else{
                try {
                    System.out.println("Client selected");
                    // We will pass in the new socket here as to stop needing to have error handling both at the constructor and the initialisation here
                    Client client = new Client(new Socket("localhost", Program.TARGET_PORT));

                    while(true){
                        if(!client.fileStreamInProgress){
                            input = scanner.nextLine();

                            String[] commandPath = input.split(" ");
                            
                            if(commandPath.length != 2){
                                System.out.println("Invalid Command");
                            }else{
                                String command = commandPath[0];
                                String path = commandPath[1];

                                switch (command) {
                                    case "cd" -> {
                                        if(client.confirmPathIsCurrentDirectory(path)) client.requestDirectoriesFromPath(client.getFullDirectoryPath(path));
                                        else System.out.println("Not a valid directory!");
                                    }
                                    case "download" -> {
                                        if(client.confirmPathIsCurrentFile(path)) client.requestFileFromPath(client.getFullFilePath(path));
                                        else System.out.println("Not a valid file!");
                                    }
                                    default -> System.out.println("Invalid Command");
                                }
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}