import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * RequestManager, the first of Coordinator threads, processes requests from participants.
 */
public class RequestManager extends Thread{

    private static final int ssPort = 4780;
    private static ServerSocket ss;
    static Socket sock = null;
    public static HashMap<Integer, ParticipantConfig> participant_list = new HashMap<>();
    public static int incomingPort;
    public static int td;

    // Default constructor
    public RequestManager(HashMap<Integer,ParticipantConfig> participant_list, int td, int incomingPort) throws IOException {

        ss = new ServerSocket(ssPort);
        this.participant_list = participant_list;
        this.incomingPort = incomingPort;
        this.td = td;
    }

    @Override
    public void run() {
        try {

            // Track participants
            int connections = 0;
            System.out.println(">_Server Listening...");

            while(true) {
                // Accept new conn
                sock = ss.accept();
                connections++;
                //take in and split request around ':'
                ObjectInputStream inputStream = new ObjectInputStream(sock.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(sock.getOutputStream());
                String whole_request = (String) inputStream.readObject();
                String[] request = whole_request.split(":");

                switch(request[0]){

                    case "register":
                        // Transmit port and add new participant to list
                        outputStream.writeObject(incomingPort);
                        participant_list.put(Integer.parseInt(request[3]),new ParticipantConfig(request[2],Integer.parseInt(request[3]), Integer.parseInt(request[1]),sock));
                        System.out.println(">_PID " + request[3] +" added to multicast group.");
                        break;

                    case "deregister":
                        // Simply change users status to deregistered NOTE: doesn't remove from list
                        String given_ip = request[1];
                        int given_pid = Integer.parseInt(request[2]);
                        // loop through all participants
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "deregistered";
                                participant.sock.close();
                            }
                        }
                        System.out.println(">_PID " + given_pid +" deregistered at address " + given_ip);
                        break;

                    case "disconnect":
                        // Simply change users status to disconnected so system can build time queue
                        given_ip = request[1];
                        given_pid = Integer.parseInt(request[2]);
                        // loop through all participants
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "disconnected";
                                participant.disconnectTime = System.currentTimeMillis();
                            }
                        }
                        System.out.println(">_PID " + given_pid +" disconnected at address " + given_ip);
                        break;

                    case "reconnect":
                        // reconnect at specified port number
                        given_ip = request[2];
                        given_pid = Integer.parseInt(request[3]);
                        // loop through all participants
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(!participant.status.equals("deregistered") && participant.pid == given_pid)
                            {
                                participant.status="active";
                                participant.port = Integer.parseInt(request[1]);
                                System.out.println(">_PID " + given_pid +" reconnected to multicast group at address " + given_ip);

                                //send all missed messages
                                while(!participant.temporalQ.isEmpty()){

                                    String message = participant.temporalQ.poll();
                                    sock = new Socket(participant.ip, participant.port);
                                    outputStream = new ObjectOutputStream(sock.getOutputStream());
                                    outputStream.writeObject(message);
                                }
                            }
                        }
                        break;
                }
            }
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
