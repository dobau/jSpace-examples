import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.Template;
import org.jspace.Tuple;

public class Client {

    public static void main(String args[]) throws Exception {
    	Space lobby = new RemoteSpace("tcp://localhost:31417/lobby?keep");
    	
    	user("Alice", lobby);
    	user("Bob", lobby);
    	user("Trend", lobby);
    }
    
    private static void user(String me, Space lobby) throws InterruptedException {
    	Tuple t;
    	
    	lobby.put(me, "QueryP", new Template("board", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	t = new Tuple( lobby.get(new ActualField("reply"), new FormalField(String.class)) );
    	if ("allowed".equals(t.getElementAt(1))) {
    		t = new Tuple( lobby.get(new ActualField(me), new ActualField("result"), new FormalField(Object.class)) );
    		System.out.printf("%s: I read %s on Alice's board\n", me, t.getElementAt(2));
    	}

    	lobby.put(me, "QueryP", new Template("inbox", "Alice", new FormalField(String.class), new FormalField(String.class)));
    	t = new Tuple( lobby.get(new ActualField("reply"), new FormalField(String.class)) );
    	if ("allowed".equals(t.getElementAt(1))) {
    		t = new Tuple( lobby.get(new ActualField(me), new ActualField("result"), new FormalField(Object.class)) );
    		System.out.printf("%s: I read %s on Alice's inbox\n", me, t.getElementAt(2));
    	}
		
	}
	
}