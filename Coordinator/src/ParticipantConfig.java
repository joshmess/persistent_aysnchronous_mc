import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/*
 * This class is used as a blueprint for the configuration of a single participant. Note status is active || disconnected || deregistered
 */
public class ParticipantConfig {

    String ip;
    int port;
    int pid;
    String status;
    long disconnectTime = -1;
    static Socket sock = null;
    static Queue<String> temporalQ = new LinkedList<>();

    // Default constructor
    public ParticipantConfig(String ip, int pid, int port, Socket sock) {

        this.ip = ip;
        this.port = port;
        this.pid = pid;
        this.sock = sock;
        this.status ="active";

    }
}
