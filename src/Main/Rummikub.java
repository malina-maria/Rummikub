package Main;

import model.GameException;
import model.HumanPlayer;
import model.Player;
import model.SmartComputerPlayer;

import java.util.Scanner;

public class Rummikub {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws GameException {
        Player player1;
        Player player2;
        System.out.println("Rummikub");
        System.out.println("Add player no. 1!");
        String name = scanner.nextLine();
        if (name.equalsIgnoreCase("Smart")){
            player1 = new SmartComputerPlayer("SmartComputer");
        } else {player1 = new HumanPlayer(name);}
        System.out.println("Add player no. 2!");
        name = scanner.nextLine();
        if (name.equalsIgnoreCase("Smart")){
            player2 = new SmartComputerPlayer("SmartComputer");
        } else {player2 = new HumanPlayer(name);}

        LocalGame game = new LocalGame(new Player[]{player1, player2});
        game.startGame();
    }
}
