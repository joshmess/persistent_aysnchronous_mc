import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.*;
import java.util.*;

/*
 * The MCReceiver class takes in MC messages and stores them to the messageQ before transmission.
 */
public class MCReceiver extends Thread {

    ArrayList<ParticipantConfig> participant_list;
    int td;
    Queue<String> messageQ;
    private static ServerSocket server;
    static Socket socket = null;

    public MCReceiver(ArrayList<ParticipantConfig> participants, int td, Queue<String> messageQ, int incomingMessagePort) throws IOException {

        this.participant_list = participants;
        this.td = td;
        this.messageQ = messageQ;
        server = new ServerSocket(incomingMessagePort);
    }


    public void run() {
        try {
            while(true) {

                while(participant_list.isEmpty()){
                    Thread.sleep(1000);
                }
                socket = server.accept();
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                String message = (String) inputStream.readObject();
                System.out.println(">_MC Message Recieved.");
                messageQ.add(message);
                socket.close();
            }

        }catch (InterruptedException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
