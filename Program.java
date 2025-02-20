import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Program {
    
    public static int DATA_TARGET_PORT = 5000;
    public static int OBJECT_TARGET_PORT = 5001;
	public static final int BUFFER_SIZE = 32 * 1024; //32KB 

    private static User user;
    private final static Scanner inputScanner = new Scanner(System.in);
    private static String input = "";

    public static void main(String[] args) {
        
        // Register whether to create a host user or client user
        while(!input.equals("c") && !input.equals("h")){
            System.out.println("Host(h) or Client(c)?");
            input = inputScanner.nextLine();
        }

        if(input.equals("h")) intialiseUserAsHost();
        else if(input.equals("c")) intialiseUserAsClient();
    }

    private static void intialiseUserAsHost() {
        try {
            user = new Host(new ServerSocket(DATA_TARGET_PORT), new ServerSocket(OBJECT_TARGET_PORT));
            
            input = "";
            while(!input.equals("close")){
                input = inputScanner.nextLine();
            } 

            //user.ShutDown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void intialiseUserAsClient() {
        user = new Client();
        Client client = (Client) user;

        input = "";
        while(!input.equals("close")){
            input = inputScanner.nextLine();
            client.sendObject(input);
        }

        //user.ShutDown();
    }

    
}