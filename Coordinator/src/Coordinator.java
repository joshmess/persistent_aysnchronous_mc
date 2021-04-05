import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/*
 * The Coordinator class drives the MC coordinator and starts all sub-processes.
 */
public class Coordinator {

    public static Queue<String> messageQ = new LinkedList<>();
    public static HashMap<Integer,ParticipantConfig> participant_list = new HashMap<>();
    public static int incomingPort;
    public static int td;

    // Start all threads
    private static void start(RequestManager rm, MCReceiver mcr, MCTransmitter mcf ){
        rm.start();
        mcf.start();
        mcr.start();
    }
    public static void main(String[] args) throws IOException {

        // Parse args
        List<String> configFile = Files.readAllLines(Paths.get(args[0]));

        incomingPort = Integer.parseInt(configFile.get(0));
        td = Integer.parseInt(configFile.get(1));

        RequestManager rm = new RequestManager(participant_list, td, incomingPort);
        MCReceiver mcr = new MCReceiver(participant_list, td, messageQ, incomingPort);
        MCTransmitter mcf = new MCTransmitter(participant_list, td, messageQ);

        start(rm, mcr, mcf);
    }
}