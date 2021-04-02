import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class GameThread extends Thread{

    private final String ROCK = "rock";
    private final String PAPER = "paper";
    private final String SCISSOR = "scissor";
    private final String SEPARATOR = "&";


    private final BufferedReader inFromPlayer1, inFromPlayer2;

    private ClientThread player1, player2;
    private String player1Choice, player2Choice;
    private int number; // Room ID

    public GameThread(int number, ClientThread player1, ClientThread player2) throws IOException {
        this.number = number;
        this.player1 = player1;
        this.player2 = player2;

        this.player1Choice = "";
        this.player2Choice = "";

        // create a buffer reader and connect it to the client's connection socket
        this.inFromPlayer1 = new BufferedReader(new InputStreamReader(player1.getConnectionSocket().getInputStream()));
        this.inFromPlayer2 = new BufferedReader(new InputStreamReader(player2.getConnectionSocket().getInputStream()));

        startGame();
    }

    private void startGame() throws IOException {
        DataOutputStream outToClient;

        outToClient = new DataOutputStream(player1.getConnectionSocket().getOutputStream());
        outToClient.writeBytes("-Play" + "\n");

        outToClient = new DataOutputStream(player2.getConnectionSocket().getOutputStream());
        outToClient.writeBytes("-Play" + "\n");
    }

    private String getWinner() {
        ClientThread winner = null;
        if (player1Choice.equals(player2Choice))
            return "DRAW";

        else if (player1Choice.equals(ROCK)) {
            if (player2Choice.equals(SCISSOR))
                winner = player1;
            else
                winner = player2;
        }

        else if (player1Choice.equals(PAPER)) {
            if (player2Choice.equals(ROCK))
                winner = player1;
            else
                winner = player2;
        }

        else if (player1Choice.equals(SCISSOR)) {
            if (player2Choice.equals(PAPER))
                winner = player1;
            else
                winner = player2;
        }

        return winner.getPlayerName();
    }

    private void announceWinner(String winner) throws IOException {
        DataOutputStream outToClient;

        outToClient = new DataOutputStream(player1.getConnectionSocket().getOutputStream());
        outToClient.writeBytes("-Result" + SEPARATOR + winner + "\n");

        outToClient = new DataOutputStream(player2.getConnectionSocket().getOutputStream());
        outToClient.writeBytes("-Result" + SEPARATOR + winner + "\n");
    }

    // thread's run function
    public void run() {

        try {

            String player1Sentence, player2Sentence;
            DataOutputStream outToClient;

            // always read messages from client
            while (true) {

                player1Sentence = inFromPlayer1.readLine();
                player2Sentence = inFromPlayer2.readLine();

                // check the start of the message
                if (player1Sentence.startsWith("-Choice")) {
                    player1Choice = player1Sentence.split(SEPARATOR)[1];
                }

                if (player2Sentence.startsWith("-Choice")) {
                    player2Choice = player2Sentence.split(SEPARATOR)[1];
                }

                if (!(player1Choice.equals("") && player2Choice.equals("")))
                    announceWinner(getWinner());

            }


        } catch (Exception ex) {}

    }
}
