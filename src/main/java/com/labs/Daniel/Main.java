package src.main.java.com.labs.Daniel;//to run the program
//  javac -cp .:SwiftBot-API-6.0.0.jar *.java
//  java -cp .:SwiftBot-API-6.0.0.jar Main

public class Main {
    public static void main(String[] args) throws InterruptedException {
        GameController gameController = new GameController();
        gameController.startGame();

    }
}