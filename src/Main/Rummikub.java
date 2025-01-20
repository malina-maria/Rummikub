package Main;

import model.GameException;
import model.HumanPlayer;
import model.Player;

import java.util.Scanner;

public class Rummikub {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws GameException {
        System.out.println("Rummikub");
        System.out.println("Add player no. 1!");
        String name = scanner.nextLine();
        Player player1 = new HumanPlayer(name);
        System.out.println("Add player no. 2!");
        name = scanner.nextLine();
        Player player2 = new HumanPlayer(name);

        Game game = new Game(new Player[]{player1, player2});
        game.startGame();
    }
}
