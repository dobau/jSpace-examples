package client;

import java.util.Scanner;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.Tuple;

public class Client {

    public static void main(String[] args) throws Exception {
    	// default values
    	String host = "localhost";
    	String port = "31415";
    	String loungeId = "lounge";

    	if (args.length > 3) {
    		System.out.println("Too many arguments");
    		System.out.println("Usage: go run main.go [address] [port] [space]");
    		System.exit(-1);
    	}

    	if (args.length >= 1) {
    		host = args[0];
    	}

    	if (args.length >= 2) {
    		port = args[1];
    	}

    	if (args.length == 3) {
    		loungeId = args[2];
    	}
    	
    	// Connect to the chat space
    	String uri = "tcp://" + host + ":" + port + "/" + loungeId + "?keep";
    	System.out.printf("Connecting to chat space %s\n", uri);
    	Space lounge = new RemoteSpace(uri);

    	// Read name from the console
    	Scanner scanner = new Scanner(System.in);
    	System.out.print("Pick a name: ");
    	String name = scanner.nextLine().trim();

    	// enter/create a rooms
    	System.out.print("Pick a room: ");
    	
    	String roomID = scanner.nextLine().trim();
    	lounge.put("enter", name, roomID);
    	Tuple t = new Tuple( lounge.get(new ActualField("roomURI"), new ActualField(name), new ActualField(roomID), new FormalField(String.class)) );
    	uri = t.getElementAt(3).toString();
    	System.out.printf("Connecting to chat space %s\n", uri);
    	Space room = new RemoteSpace(uri);

    	// Keep sending whatever the user types
    	while (true) {
    		String message = scanner.nextLine().trim();
    		room.put(name, message);
    	}
    	
    }
}
