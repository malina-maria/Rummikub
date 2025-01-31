package Main;

import java.util.Scanner;

/**
 * The {@code RummikubNetworking} class handles networking operations 
 * for the Rummikub game in a separate thread.
 */
public class RummikubNetworking extends Thread{

    /**
     * Controller instance to manage the game logic.
     */
    public Controller controller;

    /**
     * Scanner instance to read user input.
     */
    private Scanner scanner;

    /**
     * Initializes necessary resources such as the {@code Controller} and {@code Scanner}.
     * This method is executed when the thread starts.
     */
    @Override
    public void run() {
        controller = new Controller();
        scanner = new Scanner(System.in);
    }

    /**
     * The main method to create and start a new {@code RummikubNetworking} thread.
     *
     * @param args Command-line arguments (not used in this program).
     */
    public static void main(String[] args) {
        RummikubNetworking networkingThread = new RummikubNetworking();
        networkingThread.start();
    }
}