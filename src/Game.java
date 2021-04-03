import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Game {

    private final String ROCK = "rock";
    private final String PAPER = "paper";
    private final String SCISSOR = "scissor";

    private ClientThread player1, player2;
    private String player1Choice, player2Choice;
    private int number; // Room ID

    public Game(int number) throws IOException {
        this.number = number;

        this.player1Choice = "";
        this.player2Choice = "";

        startGame();
    }

    int getNumber() { return this.number; }

    ArrayList<ClientThread> getAllPlayers() {
        ArrayList<ClientThread> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        return  players;
    }

    void addPlayer(ClientThread player) {
        if (player1 == null)
            player1 = player;
        else
            player2 = player;
    }

    void startGame() throws IOException {
        if (player1 == null || player2 == null)
            return;
        else {
            DataOutputStream outToClient;

            outToClient = new DataOutputStream(player1.getConnectionSocket().getOutputStream());
            outToClient.writeBytes("-Play" + "\n");

            outToClient = new DataOutputStream(player2.getConnectionSocket().getOutputStream());
            outToClient.writeBytes("-Play" + "\n");
        }
    }

    public void updatePlayerChoice(String name, String choice) {
        if (name.equals(player1.getPlayerName()))
            player1Choice = choice;
        else
            player2Choice = choice;
    }

    public String getWinner() {
        if (player1Choice.equals("") || player2Choice.equals(""))
            return "";
        else {
            System.out.println("P1: " + player1Choice + "\n");
            System.out.println("P2: " + player2Choice + "\n");

            ClientThread winner = null;
            if (player1Choice.equals(player2Choice))
                return "DRAW";

            else if (player1Choice.equals(ROCK)) {
                if (player2Choice.equals(SCISSOR))
                    winner = player1;
                else
                    winner = player2;
            } else if (player1Choice.equals(PAPER)) {
                if (player2Choice.equals(ROCK))
                    winner = player1;
                else
                    winner = player2;
            } else if (player1Choice.equals(SCISSOR)) {
                if (player2Choice.equals(PAPER))
                    winner = player1;
                else
                    winner = player2;
            }

            return winner.getPlayerName();
        }
    }


}
