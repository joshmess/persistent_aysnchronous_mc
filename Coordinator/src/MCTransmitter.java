import java.util.ArrayList;
import java.util.*;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.net.UnknownHostException;

/*
 * The MCForwarder Class is responsible for forwarding stored MC messages toa ll active participants.
 */
public class MCTransmitter extends Thread {

    HashMap<Integer, ParticipantConfig> participant_list;
    int td;
    Queue<String> messageQ;

    // Default constructor
    public MCTransmitter(HashMap<Integer, ParticipantConfig> participants, int td, Queue<String> messageQ) {
        this.participant_list = participants;
        this.td = td;
        this.messageQ = messageQ;
    }

    @Override
    public void run(){
        while(true){
            try{
                // Loop through queue
                String messageToCast;
                synchronized (messageQ) {
                    messageToCast = messageQ.poll();
                }

                if (messageToCast != null) {
                    for(ParticipantConfig participant : participant_list.values()){
                        if(participant.status.equals("active")){
                            //send MC
                            Socket socket = new Socket(participant.ip, participant.port);
                            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                            outputStream.writeObject(messageToCast);
                        } else if(participant.status.equals("disconnected")){

                            // Add to participants temporalQ
                            participant.temporalQ.add(messageToCast);

                            //Check if td is up so the temporal bound is not exceeded
                            long diff = System.currentTimeMillis() - participant.disconnectTime;
                            if(diff/100 >= td){
                                participant.status = "deregistered";
                            }
                        }
                    }
                }
            }catch(InterruptedException | IOException e){
                e.printStackTrace();
            }
        }
    }

}


