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

    static ServerSocket server;
    static Socket socket = null;
    HashMap<Integer, ParticipantConfig> participant_list;
    int td;
    Queue<String> messageQ;

    // Default constructor
    public MCReceiver(HashMap<Integer, ParticipantConfig> participant_list, int td, Queue<String> messageQ, int incomingPort) throws IOException {

        this.participant_list = participant_list;
        this.td = td;
        this.messageQ = messageQ;
        server = new ServerSocket(incomingPort);
    }

    @Override
    public void run() {
        try {
            while(true) {

                socket = server.accept();
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                String message = (String) inputStream.readObject();
                System.out.println(">_MC Message Recieved.");
                messageQ.add(message);
                socket.close();
            }

        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
