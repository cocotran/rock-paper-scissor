import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {

    private final String ROCK = "rock";
    private final String PAPER = "paper";
    private final String SCISSOR = "scissor";

    // The ClientServiceThread class extends the Thread class and has the following parameters
    private String name; // client name
    private int number; // Client ID
    private int gameNumber; // Game ID
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
                    Server.playersList.add(name);
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
                    String playerName = clientSentence.split(SEPARATOR)[1];
                    gameNumber = Server.createNewGame();

                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeBytes("-CreateGame" + SEPARATOR + gameNumber + "\n");

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(playerName)) {
                            Server.Clients.get(i).gameNumber = this.gameNumber;

                            outToClient = new DataOutputStream(Server.Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-CreateGame" + SEPARATOR + gameNumber + "\n");
                        }
                    }
                }

                else if (clientSentence.startsWith("-Deny")) { // Create a new game
                    String playerName = clientSentence.split(SEPARATOR)[1];

                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeBytes("-StopGame" + "\n");

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(playerName)) {
                            outToClient = new DataOutputStream(Server.Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-StopGame" + "\n");
                        }
                    }
                }

                else if (clientSentence.startsWith("-Game")) {  // Players join game
                    String gameID = clientSentence.split(SEPARATOR)[1];

                    Game game = Server.getGameByID(Integer.parseInt(gameID));

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).number == number) {
                            game.addPlayer(Server.Clients.get(i));
                            game.startGame();
                        }
                    }

                    Server.playersList.remove(name);
                    Server.updatePlayersList();
                }

                else if (clientSentence.startsWith("-Choice")) { // Game choice
                    String choice = clientSentence.split(SEPARATOR)[1];

                    Game game = Server.getGameByID(gameNumber);

                    game.updatePlayerChoice(name, choice);

                    if (!game.getWinner().equals(""))
                        Server.announceWinner(game);
                }

                else if (clientSentence.startsWith("-Stop")) { // Game choice
                    String playerName = clientSentence.split(SEPARATOR)[1];

                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeBytes("-StopGame" + "\n");

                    for (int i = 0; i < Server.Clients.size(); i++) {
                        if (Server.Clients.get(i).name.equals(playerName)) {
                            outToClient = new DataOutputStream(Server.Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-StopGame" + "\n");
                            Server.playersList.add(playerName);
                        }
                    }

                    for (int i = 0; i < Server.Games.size(); i++) {
                        if (Server.Games.get(i).getNumber() == gameNumber) {
                            System.out.println("Remove room ID: " + Server.Games.get(i).getNumber());
                            Server.Games.remove(i);
                        }
                    }

                    Server.playersList.add(name);

                    Server.updatePlayersList();
                }


            }


        } catch (Exception ex) { }

    }
}
