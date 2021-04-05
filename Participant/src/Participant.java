import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * The Participant class drives MC participants and forwards on their requests.
 */
public class Participant {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        //Parse args
        List<String> configFile = Files.readAllLines(Paths.get(args[0]));
        int pid = Integer.parseInt(configFile.get(0));
        String logFile = configFile.get(1);
        String server = configFile.get(2).split(":")[0];
        int serverPort = Integer.parseInt(configFile.get(2).split(":")[1]);

        //create necessary locals
        Scanner s = new Scanner(System.in);
        int outgoingPort = 0;
        int MCPort = 0;
        String currentWorkingDirectory = "MCparticipant >_";
        Socket socket = null;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        String input = "";
        String myIp = Inet4Address.getLocalHost().getHostAddress();
        boolean isDisconnected = false;
        boolean isRegistered = false;
        MC multicast_thread = null;

        while(!input.equals("quit")){
            System.out.print(currentWorkingDirectory);
            input = s.nextLine();
            String[] commandAndValue = input.split(" ");

            switch(commandAndValue[0]) {

                case "register":

                    isRegistered = true;
                    MCPort = Integer.parseInt(commandAndValue[1]);
                    socket = new Socket(server, serverPort);
                    outputStream = new  ObjectOutputStream(socket.getOutputStream());
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    outputStream.writeObject(commandAndValue[0] + " "+ myIp + " " + pid + " " + commandAndValue[1]);
                    outgoingPort = (int)inputStream.readObject();
                    multicast_thread = new MC(server, MCPort, logFile);
                    multicast_thread.start();
                    break;

                case "deregister":

                    if(isRegistered){
                        socket = new Socket(server, serverPort);
                        outputStream = new  ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        outgoingPort = 0;
                        outputStream.writeObject(commandAndValue[0] + " " + pid);
                        isRegistered = false;
                        multicast_thread.end();
                        multicast_thread.interrupt();
                        outputStream.close();
                        inputStream.close();
                        socket.close();

                    }
                    else
                        System.out.println(">_Not in MC group.");
                    break;

                case "disconnect":

                    if(isRegistered){

                        socket = new Socket(server, serverPort);
                        outputStream = new  ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());

                        outputStream.writeObject(commandAndValue[0] + " " + pid);
                        isDisconnected = true;

                    }
                    else
                        System.out.println(">_NOT in MC group.");

                    break;

                case "reconnect":

                    if(isDisconnected){
                        socket = new Socket(server, serverPort);
                        outputStream = new  ObjectOutputStream(socket.getOutputStream());
                        inputStream = new ObjectInputStream(socket.getInputStream());
                        myIp = Inet4Address.getLocalHost().getHostAddress();
                        outputStream.writeObject("reconnect " + pid + " "+ commandAndValue[1]);
                        MCPort = Integer.parseInt(commandAndValue[1]);
                        multicast_thread = new MC(server, MCPort, logFile);
                        multicast_thread.start();
                        isDisconnected = false;
                    }
                    else
                        System.out.println(">_Already connected or not in MC group.");
                    break;

                case "msend":
                    if(isRegistered && !isDisconnected){
                        socket = new Socket(server, outgoingPort);
                        outputStream = new  ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(input.substring(6));
                        Thread.sleep(10);

                    }
                    else if (!isRegistered){

                        System.out.println(">_Not registered with the MC group.");
                    }
                    else if (isDisconnected)
                        System.out.println(">_Reconnect to the MC group first.");
                    break;

            }

        }

        System.out.println("Have a nice day!");
        System.exit(0);

    }

}

