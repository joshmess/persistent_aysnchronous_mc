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


    public MCTransmitter(HashMap<Integer, ParticipantConfig> participants, int td, Queue<String> messageQ) {

        this.participant_list = participants;
        this.td = td;
        this.messageQ = messageQ;

    }

    public void run(){

        while(true){

            try{
                Thread.sleep(10);

                while(!messageQ.isEmpty()){

                    String messageToCast = messageQ.poll();
                    for(ParticipantConfig participant : participant_list.values()){
                        if(participant.status.equals("active")){

                            Socket socket = new Socket(participant.ip, participant.port);
                            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                            outputStream.writeObject(messageToCast);
                        }
                        else if(participant.status.equals("disconnected")){

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


