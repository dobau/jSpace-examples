import java.util.List;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;
import org.jspace.Template;
import org.jspace.Tuple;

public class Server {

	public static void main(String[] args) throws Exception {
    	// Space with the data to be protected
    	Space space = new SequentialSpace();
    	space.put("inbox", "Alice", "Bob", "Hello");
    	space.put("board", "Alice", "Charlie", "Hello");
    	
    	// Space with access control rules
    	Space rules = new SequentialSpace();
    	
    	// Rules for the inbox
    	rules.put("allow", "Alice", "Query", new Template("inbox", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Alice", "QueryP", new Template("inbox", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "Query", new Template("inbox", "Bob", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "QueryP", new Template("inbox", "Bob", new FormalField(String.class), new FormalField(String.class)));

    	// Rules for the board
    	rules.put("allow", "Alice", "Query", new Template("board", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Alice", "Query", new Template("board", "Bob", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Alice", "QueryP", new Template("board", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Alice", "QueryP", new Template("board", "Bob", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "Query", new Template("board", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "Query", new Template("board", "Bob", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "QueryP", new Template("board", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	rules.put("allow", "Bob", "QueryP", new Template("board", "Bob", new FormalField(String.class), new FormalField(String.class)));

    	// Space to interact with users
    	Space lobby = new SequentialSpace();
    	
    	
    	SpaceRepository spaceRepository = new SpaceRepository();
    	spaceRepository.addGate("tcp://localhost:31415/space?keep");
    	spaceRepository.add("space", space);
    	
    	SpaceRepository rulesRepository = new SpaceRepository();
    	rulesRepository.addGate("tcp://localhost:31416/space?keep");
    	rulesRepository.add("rules", rules);
    	
    	SpaceRepository lobbyRepository = new SpaceRepository();
    	lobbyRepository.addGate("tcp://localhost:31417/space?keep");
    	lobbyRepository.add("lobby", lobby);

    	
    	while (true) {
    		Object[] obj = (Object[]) lobby.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Template.class));
    		
    		Tuple tuple = new Tuple(obj);

        	String subject = tuple.getElementAt(0).toString();
        	String action = tuple.getElementAt(1).toString();
        	Template template = (Template) tuple.getElementAt(2);
    		
    		System.out.printf("%s wants to do %s(%s)\n", subject, action, template);
    		System.out.printf("Checking rules...\n");
    		String decision = check(rules, subject, action, template);
    		
    		if (decision != "permit") {
    			System.out.printf("Permission denied.\n");
    			lobby.put("reply", "denied");
    			continue;
    		}
    		
    		System.out.printf("Permission granted.\n");
    		lobby.put("reply", "allowed");
    		System.out.printf("Performing action...\n");
    		
    		Object[] result;
    		List<Object[]> resultList;
    		
    		try {
	    		switch(action) {
	    		case "Query":
	    			result = space.query(template.getFields());
	    			lobby.put(subject, "result", result);
	    			break;
	    		case "QueryP":
	    			result = space.queryp(template.getFields());
	    			
	    			System.out.println("Providing result...");
	    			lobby.put(subject, "result", result);
	    			break;
	    		case "Get":
	    			result = space.get(template.getFields());
	    			System.out.println("Providing result...");
	    			lobby.put(subject, "result", result);
	    			break;
	    		case "GetP":
	    			// TODO: Check if this is a bug (https://github.com/pSpaces/goSpace-examples/blob/5376f11647db05f7595fb19dbd01e21bdd793762/tutorial/access-control-0/main.go#L102)
	    			result = space.query(template.getFields());
	    			System.out.println("Providing result...");
	    			lobby.put(subject, "result", result);
	    			break;
	    		case "Put":
	    			// TODO: Check if this is a bug (https://github.com/pSpaces/goSpace-examples/blob/5376f11647db05f7595fb19dbd01e21bdd793762/tutorial/access-control-0/main.go#L106)
	    			result = space.query(template.getFields());
	    			System.out.println("Providing result...");
	    			lobby.put(subject, "result", result);
	    			break;
	    		case "QueryAll":
	    			resultList = space.queryAll(template.getFields());
	    			System.out.println("Providing result...");
	    			lobby.put("result", resultList);
	    			break;
	    		case "GetAll":
	    			resultList = space.getAll(template.getFields());
	    			System.out.println("Providing result...");
	    			lobby.put("result", resultList);
	    			break;
	    		default:
	    			System.out.println("Providing result...");
	    			lobby.put("result", null);
	    		}
    		} catch(Exception ex) {
    			System.err.println("Error: "+ex.getMessage());
    			lobby.put("result", "ko");    			
    		}
    	}

    	// TODO: What is it good for?
    	//space.query(new ActualField("stop"));
	}
	
	private static String check(Space rules, String subject, String action, Template template) throws InterruptedException {
		String decision = "deny";

		Object result = rules.queryp(new FormalField(String.class), new ActualField(subject), new ActualField(action), new ActualField(template));
		
		if (result != null) {
			decision = "permit";
		}
		
		return decision;
	}
	
}
