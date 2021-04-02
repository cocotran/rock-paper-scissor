import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    // The ClientServiceThread class extends the Thread class and has the following parameters
    private String name;
    private int number; // client name
    private Socket connectionSocket; // client connection socket


    // constructor function
    public ClientThread(int number, Socket connectionSocket) {

        this.number = number;
        this.connectionSocket = connectionSocket;
        this.name = "";

    }

    // thread's run function
    public void run() {

        try {

            // create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String clientSentence;
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());;

            // always read messages from client
            while (true) {

                clientSentence = inFromClient.readLine();
                System.out.println(clientSentence);

                // check the start of the message

                if (clientSentence.startsWith("-Remove")) { // Remove Client
                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).number == number) {
                            Server.Clients.remove(i);
                            return;
                        }
                    }
                }

                else if (clientSentence.startsWith("-Join")) { // Check client name
                    String []client = clientSentence.split(",");

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(client[1])) {
                            outToClient.writeBytes("-NameTaken" + "\n");
                            return;
                        }
                    }

                    outToClient.writeBytes("-Joined," + client[1] + "\n");
                    name = client[1];
                }





            }


        } catch (Exception ex) {
        }

    }
}
