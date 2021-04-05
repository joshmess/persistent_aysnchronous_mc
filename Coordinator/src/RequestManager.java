import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * RequestManager, the first of Coordinator threads, processes requests from participants.
 */
public class RequestManager extends Thread{

    private static final int ssPort = 4780;
    private static ServerSocket ss;
    static Socket sock = null;
    public static ArrayList<ParticipantConfig> participant_list = new ArrayList<>();
    public static int incomingPort;
    public static int td;

    public RequestManager(ArrayList<ParticipantConfig> participant_list, int td, int incomingPort) throws IOException {

        ss = new ServerSocket(ssPort);
        this.participant_list = participant_list;
        this.incomingPort = incomingPort;
        this.td = td;
    }

    public void run() {
        try {

            int connections = 0;
            System.out.println(">_Server Listening...");

            while(true) {

                sock = ss.accept();
                connections++;
                System.out.println(">_Participant no." + connections +"connected");
                //take in request
                ObjectInputStream inputStream = new ObjectInputStream(sock.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(sock.getOutputStream());
                String whole_request = (String) inputStream.readObject();
                String[] request = whole_request.split(" ");

                switch(request[0]){

                    case "register":
                        outputStream.writeObject(incomingPort);
                        participant_list.add(new ParticipantConfig(request[1],Integer.parseInt(request[2]), Integer.parseInt(request[3]),sock));
                        System.out.println(">_PID " + request[2] +" added to multicast group.");
                        break;

                    case "disconnect":
                        int given_pid = Integer.parseInt(request[1]);
                        for(ParticipantConfig participant : participant_list)
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "disconnected";
                            }
                        }
                        System.out.println(">_PID " + request[1] +" disconnected");
                        break;

                    case "deregister":
                        given_pid = Integer.parseInt(request[1]);
                        for(ParticipantConfig participant : participant_list)
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "deregistered";
                                participant.sock.close();
                            }
                        }
                        System.out.println(">_PID " + request[1] +" deregistered");
                        break;

                    case "reconnect":

                        given_pid = Integer.parseInt(request[1]);
                        for(ParticipantConfig participant : participant_list)
                        {
                            if(!participant.status.equals("deregistered") && participant.pid == given_pid)
                            {
                                participant.status="active";
                                participant.port = Integer.parseInt(request[2]);
                                System.out.println(">_PID " + request[1] +" reconnected to multicast group");

                                //send all missed messages
                                while(!participant.temporalQ.isEmpty()){

                                    String message = participant.temporalQ.poll();
                                    sock = new Socket(participant.ip, participant.port);
                                    outputStream = new ObjectOutputStream(sock.getOutputStream());
                                    outputStream.writeObject(message);
                                    System.out.println(message);
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
