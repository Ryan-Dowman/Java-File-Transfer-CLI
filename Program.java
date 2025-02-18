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
                    // We will pass in the new socket here as to stop needing to have error handling both at the construcctor and the initialisation here
                    new Client(new Socket("localhost", Program.TARGET_PORT));
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}