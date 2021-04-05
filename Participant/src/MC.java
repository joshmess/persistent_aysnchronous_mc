import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.io.*;

/*
 * The Multicast (MC) class ia a thread that is responsible for handling multicasting at the participant side.
 */
public class MC extends Thread{

    String server;
    int MCport;
    String logfile;
    static ServerSocket ss;
    boolean leave;

    public MC(String server, int MCport, String logFile) throws  IOException {

        this.server = server;
        this.MCport = MCport;
        this.logfile = logFile;
        ss = new ServerSocket(MCport);
        leave = false;
    }

    public void run()
    {
        try{
            while(!leave){

                Socket MCsock = ss.accept();
                ObjectInputStream inputStream = new ObjectInputStream(MCsock.getInputStream());
                String message = (String)inputStream.readObject();
                log(message, logfile);

            }

        }catch(ClassNotFoundException | IOException e){
            e.printStackTrace();
        }


    }

    public void log(String message, String fileName){

        try{
            File file = new File(fileName);
            FileWriter fr = new FileWriter(file, true);
            BufferedWriter br = new BufferedWriter(fr);
            PrintWriter pr = new PrintWriter(br);
            pr.println("MC MSG >_ " + message);
            pr.close();
            br.close();
            fr.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
