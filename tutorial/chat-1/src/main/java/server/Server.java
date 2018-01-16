package server;

import java.util.HashMap;
import java.util.Map;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.Tuple;
import org.jspace.gate.KeepClientGate;

public class Server {

	public static void main(String[] args) throws Exception {

    	// Extract local host and port number from the command line
		Parameters param = new Parameters(args);

    	// Create the chat space
    	String loungeURI = "tcp://" + param.host + ":" + param.port + "/" + param.loungeID + "?keep";
    	System.out.printf("Setting up lounge space %s...\n", loungeURI);
    	Space lounge = new SequentialSpace();
    	
    	SpaceRepository spaceRepository = new SpaceRepository();
    	spaceRepository.add("lounge", lounge);
    	spaceRepository.addGate(loungeURI);
    	
    	new Thread(loungeWelcome(lounge, param.host, param.port)).start();
    	
    	lounge.get(new ActualField("stop"));
    	
    }
    
	public static Runnable loungeWelcome(final Space lounge, final String host, final String port) {
		return () -> {
			try {
				// This maps room identifiers to port numbers
				Map<String, Integer> rooms = new HashMap<>();
		
				// chartPort will be used to ensure every chat space has a unique port number
				int chatPort = Integer.parseInt(port);
				chatPort++;
				while(true) {
					String roomURI;
					
					// Process room login requests
					Tuple t = new Tuple( lounge.get(new ActualField("enter"), new FormalField(String.class), new FormalField(String.class)) );
					String who = t.getElementAt(1).toString();
					String roomID = t.getElementAt(2).toString();
					System.out.printf("%s requesting to enter %s...\n", who, roomID);
		
					if (rooms.containsKey(roomID)) {
						roomURI = "tcp://" + host + ":" + rooms.get(roomID) + "/" + roomID + "?keep";
					} else {
						System.out.printf("Creating room %s for %s...\n", roomID, who);
						rooms.put(roomID, chatPort);
						chatPort++;
						roomURI = "tcp://" + host + ":" + rooms.get(roomID) + "/" + roomID + "?keep";
						System.out.printf("Setting up chat space %s...\n", roomURI);
						Space room = new SequentialSpace();
						
						SpaceRepository roomRepository = new SpaceRepository();
						roomRepository.add("room#"+roomID, room);
						roomRepository.addGate(roomURI);
						
						new Thread(show(room, roomID)).start();
					}
					System.out.printf("Telling %s to go for room %s on uri %s\n", who, roomID, roomURI);
					lounge.put("roomURI", who, roomID, roomURI);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		};
		
	}

	private static Runnable show(Space room, String roomID) {
		return () -> {
			while(true) {
				try {
					Tuple t = new Tuple( room.get(new FormalField(String.class), new FormalField(String.class)) );
					String who = t.getElementAt(0).toString();
					String message = t.getElementAt(1).toString();
					System.out.printf("ROOM %s | %s: %s \n", roomID, who, message);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
	}

	static class Parameters {

		// default values
		public String host = "localhost";
		public String port = "31415";
		public String loungeID = "lounge";

		public Parameters(String[] args) {
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
				loungeID = args[2];
			}
		}

	}

}
