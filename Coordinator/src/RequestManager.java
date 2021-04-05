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

    public RequestManager(HashMap<Integer,ParticipantConfig> participant_list, int td, int incomingPort) throws IOException {

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
                //take in and split request around :
                ObjectInputStream inputStream = new ObjectInputStream(sock.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(sock.getOutputStream());
                String whole_request = (String) inputStream.readObject();
                String[] request = whole_request.split(":");

                switch(request[0]){

                    case "register":
                        outputStream.writeObject(incomingPort);
                        participant_list.put(Integer.parseInt(request[3]),new ParticipantConfig(request[2],Integer.parseInt(request[3]), Integer.parseInt(request[1]),sock));
                        System.out.println(">_PID " + request[3] +" added to multicast group.");
                        break;

                    case "deregister":
                        String given_ip = request[1];
                        int given_pid = Integer.parseInt(request[2]);
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "deregistered";
                                participant.sock.close();
                            }
                        }
                        System.out.println(">_PID " + request[1] +" deregistered at address " + given_ip);
                        break;
                    case "disconnect":
                        given_ip = request[1];
                        given_pid = Integer.parseInt(request[2]);
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(participant.pid == given_pid)
                            {
                                participant.status = "disconnected";
                                participant.disconnectTime = System.currentTimeMillis();
                            }
                        }
                        System.out.println(">_PID " + request[1] +" disconnected at address " + given_ip);
                        break;

                    case "reconnect":
                        given_ip = request[2];
                        given_pid = Integer.parseInt(request[3]);
                        for(ParticipantConfig participant : participant_list.values())
                        {
                            if(!participant.status.equals("deregistered") && participant.pid == given_pid)
                            {
                                participant.status="active";
                                participant.port = Integer.parseInt(request[1]);
                                System.out.println(">_PID " + given_ip +" reconnected to multicast group at address " + given_ip);

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
