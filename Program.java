import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class Program {
    
    public static int DATA_TARGET_PORT = 5000;
    public static int OBJECT_TARGET_PORT = 5001;
	public static final int BUFFER_SIZE = 32 * 1024; // 32KB 

    private static User user;
    private final static Scanner inputScanner = new Scanner(System.in);
    private static String input = "";

    public static void main(String[] args) {
        listenForConnectionType();
    }

    private static void listenForConnectionType(){
        List<String> validClientCommands = Arrays.asList("c", "client");
        List<String> validHostCommands = Arrays.asList("h", "host");
        
        while(!validClientCommands.contains(input.toLowerCase()) && !validHostCommands.contains(input.toLowerCase())){
            System.out.println("Host(h) or Client(c)?");
            input = inputScanner.nextLine();
        }

        if(validHostCommands.contains(input.toLowerCase())) intialiseUserAsHost();
        else if(validClientCommands.contains(input.toLowerCase())) intialiseUserAsClient();
        else System.out.println("Failure to recognise user type");
    }

    private static void intialiseUserAsHost() {
        try {
            user = new Host(new ServerSocket(DATA_TARGET_PORT), new ServerSocket(OBJECT_TARGET_PORT));
            Host host = (Host) user;

            Map<String, Consumer<String>> hostCommandFunctionMap = Map.of(
                "boot", _ -> host.bootClient(),
                "exit", _ -> System.out.println("")
            );

            try {
                String ipAddress = InetAddress.getLocalHost().getHostAddress(); 
                System.out.println("Hosting on " + ipAddress + " (provide to client program)");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            handleUserInputs(hostCommandFunctionMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void intialiseUserAsClient() {
        
        input = "";

        System.out.println("Input IP Address for host: ");
        input = inputScanner.nextLine();
        
        user = new Client(input);
        Client client = (Client) user;
        
        Map<String, Consumer<String>> clientCommandFunctionMap = Map.of(
            "cd", client::sendObject,
            "download", client::requestFileDownload,
            "exit", _ -> System.out.println("")
        );

        handleUserInputs(clientCommandFunctionMap);
    }

    private static  void handleUserInputs(Map<String, Consumer<String>> commandFunctionMap){
        
        input = "";
        while(!input.toLowerCase().equals("exit")){
            input = inputScanner.nextLine();
            String[] splitCommandString = input.split(" ");
            
            if(splitCommandString.length > 2){
                System.out.println("Invalid Command!");
                continue;
            } 

            String inputCommand = splitCommandString[0];
            String inputTarget = splitCommandString.length == 2 ? splitCommandString[1] : "";

            commandFunctionMap.getOrDefault(inputCommand.toLowerCase(), _ -> System.out.println("Invalid Command!")).accept(inputTarget);
        }

        user.ShutDown();
    }
}