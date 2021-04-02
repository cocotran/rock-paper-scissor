import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread extends Thread {

    // The ClientServiceThread class extends the Thread class and has the following parameters
    private String name; // client name
    private int number; // Client ID
    private Socket connectionSocket; // Client connection socket


    // constructor function
    public ClientThread(int number, Socket connectionSocket) {
        this.number = number;
        this.connectionSocket = connectionSocket;
        this.name = "";
    }

    ClientThread(ClientThread ct) {
        this.number = ct.number;
        this.connectionSocket = ct.connectionSocket;
        this.name = ct.name;
    }

    String getPlayerName() { return this.name; }

    Socket getConnectionSocket() { return this.connectionSocket; }

    // thread's run function
    public void run() {

        final String SEPARATOR = "&";

        try {

            // create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String clientSentence;
            DataOutputStream outToClient;

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
                    String []client = clientSentence.split(SEPARATOR);
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(client[1])) {
                            outToClient.writeBytes("-NameTaken" + "\n");
                            return;
                        }
                    }

                    outToClient.writeBytes("-Joined" + SEPARATOR + client[1] + "\n");
                    name = client[1];
                }

                else if (clientSentence.startsWith("-PlayersList")) { // Send list of connected players
                    Server.updatePlayersList();
                }

                else if (clientSentence.startsWith("-Request")) { // Send list of connected players
                    String []data = clientSentence.split(SEPARATOR);
                    String playerName = data[1];

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(playerName)) {
                            outToClient = new DataOutputStream(Server.Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-Request" + SEPARATOR + name + "\n");
                        }
                    }
                }

                else if (clientSentence.startsWith("-Accept")) { // Create a new game
                    ClientThread player1 = null, player2 = null;

                    // Get player 1
                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).number == number)
                            player1 = new ClientThread(Server.Clients.get(i));
                    }

                    // Get player 2
                    String playerName = clientSentence.split(SEPARATOR)[1];
                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(playerName))
                            player2 = new ClientThread(Server.Clients.get(i));
                    }

                    Server.createNewGame(player1, player2);
                }




            }


        } catch (Exception ex) { }

    }
}
