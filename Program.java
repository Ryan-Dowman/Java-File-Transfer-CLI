import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Program {
    
    public static int TARGET_PORT = 5000;
	public static final int BUFFER_SIZE = 32 * 1024; //32KB 

    private static User user;

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        String input = "";
        
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
            user = new Host(new ServerSocket(TARGET_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void intialiseUserAsClient() {
        user = new Client();
    }

    
}