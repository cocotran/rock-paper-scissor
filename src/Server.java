import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private final static String SEPARATOR = "&";

    // Array of type ClientServiceThread, for all connected clients
    public static ArrayList<ClientThread> Clients = new ArrayList<ClientThread>();
    private static int clientCount = 0;

    public static ArrayList<Game> Games = new ArrayList<Game>();
    private static int gameCount = 0;

    public static ArrayList<String> playersList = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        // Create the GUI frame and components
        JFrame frame = new JFrame ("RPS Game Server");
        frame.setLayout(null);
        frame.setBounds(100, 100, 300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JLabel connectionStatusLabel = new JLabel("No Clients Connected");
        connectionStatusLabel.setBounds(80, 30, 200, 30);
        connectionStatusLabel.setForeground(Color.red);
        frame.getContentPane().add(connectionStatusLabel);

        // create the welcoming server's socket
        ServerSocket welcomeSocket = new ServerSocket(6789);

        // thread to always listen for new connections from clients
        new Thread (new Runnable(){ @Override
        public void run() {

            Socket connectionSocket;
            DataOutputStream outToClient;

            while (!welcomeSocket.isClosed()) {

                try {
                    // when a new client connect, accept this connection and assign it to a new connection socket
                    connectionSocket = welcomeSocket.accept();

                    // create a new output stream and send the message "You are connected" to the client
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                    outToClient.writeBytes("-Connected\n");

                    clientCount++;

                    // add the new client to the client's array
                    Clients.add(new ClientThread(clientCount, connectionSocket));
                    // start the new client's thread
                    Clients.get(Clients.size() - 1).start();

                }
                catch (Exception ex) {

                }

            }

        }}).start();



        // thread to always get the count of connected clients and update the label
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                while (true) {

                    if (Clients.size() > 0)
                    {
                        if (Clients.size() == 1)
                            connectionStatusLabel.setText("1 Client Connected");
                        else
                            connectionStatusLabel.setText(Clients.size() + " Clients Connected");

                        connectionStatusLabel.setForeground(Color.blue);
                    }
                    else { //if there are no clients connected, print "No Clients Connected"

                        connectionStatusLabel.setText("No Clients Connected");
                        connectionStatusLabel.setForeground(Color.red);

                    }


                    Thread.sleep(1000);

                }

            } catch (Exception ex) {}

        }}).start();


        frame.setVisible(true);

    }


    static void updatePlayersList() throws IOException {
        DataOutputStream outToClient;

        String list = String.join(",", playersList);

        for (int i=0; i < Clients.size(); i++) {
            outToClient = new DataOutputStream(Clients.get(i).getConnectionSocket().getOutputStream());
            outToClient.writeBytes("-PlayersList" + SEPARATOR + list + "\n");
        }
    }


    static int createNewGame() throws IOException {
        int gameID = generateNewGameID();

        Game game = new Game(gameID);
        Games.add(game);

        System.out.println("New game started: " + gameID);

        return gameID;
    }

    static Game getGameByID(int ID) {
        for (int i = 0; i < Games.size(); i++) {
            if (Games.get(i).getNumber() == ID)
                return Games.get(i);
        }
        return null;
    }

    static void announceWinner(Game game) throws IOException {
        DataOutputStream outToClient;

        ArrayList<ClientThread> players = game.getAllPlayers();
        for (int i = 0; i < players.size(); i++) {
            outToClient = new DataOutputStream(players.get(i).getConnectionSocket().getOutputStream());
            outToClient.writeBytes("-Result" + SEPARATOR + game.getWinner() + "\n");
        }
        game.startNewGame();
    }

    public static int generateNewGameID() { return gameCount++; }

}