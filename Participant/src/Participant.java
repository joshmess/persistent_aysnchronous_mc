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
        String pwd = "MCparticipant >_";
        Socket socket = null;
        ObjectOutputStream outputStream;
        ObjectInputStream inputStream;
        String input = "";
        String myIp = Inet4Address.getLocalHost().getHostAddress();
        boolean isDisconnected = false;
        boolean isRegistered = false;
        MC multicast_thread = null;

        while(!input.equals("quit")){
            System.out.print(pwd);
            input = s.nextLine();
            String[] request = input.split(" ");

            switch(request[0]) {

                case "register":

                    isRegistered = true;
                    socket = new Socket(server, serverPort);
                    outputStream = new  ObjectOutputStream(socket.getOutputStream());
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    MCPort = Integer.parseInt(request[1]);
                    outputStream.writeObject(request[0] + ":"+ MCPort + ":" + myIp + ":" + pid);
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
                        outputStream.writeObject(request[0] + ":" + myIp + ":" + pid);
                        isRegistered = false;
			multicast_thread.leave = true;
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

                        outputStream.writeObject(request[0] + ":" + myIp + ":" + pid);
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
                        MCPort = Integer.parseInt(request[1]);
                        outputStream.writeObject(request[0] + ":"+ MCPort + ":" + myIp + ":" + pid);
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
                        outputStream.writeObject(input.substring(input.indexOf(" ")+1));
                        Thread.sleep(10);
                    }
                    else if (!isRegistered){
                        System.out.println(">_Not registered with the MC group.");
                    }
                    else {
                        System.out.println(">_Problems multicasting, could be due to your status.");
                    }
                    break;

            }

        }

        System.out.println("Have a nice day!");
        System.exit(0);

    }

}

