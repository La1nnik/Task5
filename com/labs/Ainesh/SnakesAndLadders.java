package com.labs.Ainesh;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import swiftbot.SwiftBotAPI;
import swiftbot.Button;

public class SnakesAndLadders {

    HashMap<Integer, Integer> snakesMap = new HashMap<>(); // holds the snake drops
    HashMap<Integer, Integer> LADDERS = new HashMap<>();
    
    Random r_gen = new Random();
    Scanner sc = new Scanner(System.in);
    
    double currentAngle = 0.0; // keeps track of where the bot is facing right now

    public static void main(String[] args) {
        try {
            SwiftBotAPI bot_obj = SwiftBotAPI.INSTANCE;
            SnakesAndLadders G = new SnakesAndLadders();
            G.runTask3_Integration(bot_obj);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    // Helper method to handle v6.0.0 event-driven buttons synchronously
    private void waitForButton(SwiftBotAPI bot, Button targetBtn) {
        final boolean[] isPressed = {false};
        
        bot.enableButton(targetBtn, () -> {
            isPressed[0] = true;
        });

        while(isPressed[0] == false) {
            try { Thread.sleep(50); } catch (Exception ignore) { }
        }
        
        bot.disableButton(targetBtn); // cleanup
    }

    // Helper method to wait for EITHER X or Y button (for the midpoint check)
    private String waitForXorY(SwiftBotAPI bot) {
        final String[] result = {"NONE"};
        
        bot.enableButton(Button.X, () -> {
            result[0] = "X";
        });
        bot.enableButton(Button.Y, () -> {
            result[0] = "Y";
        });

        while(result[0].equals("NONE")) {
            try { Thread.sleep(50); } catch (Exception ignore) { }
        }
        
        bot.disableButton(Button.X);
        bot.disableButton(Button.Y);
        return result[0];
    }

    public void runTask3_Integration(SwiftBotAPI swiftBotInst) {
        System.out.println("========================================");
        System.out.println("        SNAKES AND LADDERS GAME         ");
        System.out.println("========================================");

        // mapping the board
        snakesMap.put(14, 4);
        snakesMap.put(24, 16);
        LADDERS.put(3, 12);
        LADDERS.put(10, 21);

        System.out.println("Snakes: 14->4, 24->16 | Ladders: 3->12, 10->21");
        System.out.println("Press Button 'Y' on the SwiftBot to start.");
        
        waitForButton(swiftBotInst, Button.Y);

        System.out.print("Enter preferred name: ");
        String P1_NAME = sc.nextLine();
        if (P1_NAME.equals("")) {
            P1_NAME = "Player"; 
        }
        
        System.out.println("Select Game Mode:");
        System.out.println("A - Normal Mode");
        System.out.println("B - Override Mode");
        System.out.print("Choice: ");
        
        String selected_mode = sc.nextLine();
        while (!selected_mode.equalsIgnoreCase("A") && !selected_mode.equalsIgnoreCase("B")) {
            System.out.print("Invalid choice. Enter A or B: ");
            selected_mode = sc.nextLine();
        }

        int p_loc = 1;
        int b_loc = 1;

        int r1 = r_gen.nextInt(6) + 1;
        int r2 = r_gen.nextInt(6) + 1;
        
        System.out.println("[" + P1_NAME + "] rolled: " + r1);
        System.out.println("[SwiftBot] rolled: " + r2);

        boolean p_turn = false;
        if (r1 >= r2) {
            p_turn = true;
            System.out.println(P1_NAME + " goes first!");
        } else {
            System.out.println("SwiftBot goes first!");
        }

        boolean stillPlayingFlag = true;

        while (stillPlayingFlag == true) {
            if (p_turn == true) {
                System.out.println("\n--- " + P1_NAME + "'s Turn ---");
                System.out.println("Press Button 'A' on bot to roll.");
                
                waitForButton(swiftBotInst, Button.A);

                int diceroll = r_gen.nextInt(6) + 1;
                int tempNext = p_loc + diceroll;

                if (tempNext > 25) {
                    System.out.println("Rolled " + diceroll + " but it is too high. Staying at " + p_loc);
                } else {
                    System.out.println("Rolled " + diceroll + " and moved to " + tempNext);
                    p_loc = tempNext;
                    
                    if (snakesMap.containsKey(p_loc)) {
                        p_loc = snakesMap.get(p_loc);
                        System.out.println("Hit a snake! Sliding down to " + p_loc);
                    } else if (LADDERS.containsKey(p_loc)) {
                        p_loc = LADDERS.get(p_loc);
                        System.out.println("Hit a ladder! Climbing up to " + p_loc);
                    }
                }

                if (p_loc == 25) {
                    System.out.println("\n*** " + P1_NAME.toUpperCase() + " WINS! ***");
                    stillPlayingFlag = false;
                }
            } 
            else {
                System.out.println("\n--- SwiftBot's Turn ---");
                int b_roll = r_gen.nextInt(6) + 1;
                int nxtBotPos = b_loc + b_roll;

                if (selected_mode.equalsIgnoreCase("B")) {
                    System.out.println("Bot rolled " + b_roll + " from position " + b_loc);
                    
                    int MAX_ALLOWED = b_loc + 5; // limit to 5 squares for assignment 3 requirement
                    if (MAX_ALLOWED > 25) { MAX_ALLOWED = 25; }
                    
                    boolean isInputOk = false;
                    while(isInputOk == false) {
                        System.out.print("Mode B: Enter target (" + (b_loc + 1) + "-" + MAX_ALLOWED + "), or 0 to keep roll: ");
                        try {
                            String userin = sc.nextLine();
                            int override_val = Integer.parseInt(userin);
                            
                            if (override_val >= b_loc + 1 && override_val <= MAX_ALLOWED) {
                                nxtBotPos = override_val;
                                System.out.println("Override accepted. Moving to: " + nxtBotPos);
                                isInputOk = true;
                            } else if (override_val == 0) {
                                System.out.println("Keeping the original roll.");
                                isInputOk = true;
                            } else {
                                System.out.println("Error: Target is out of bounds.");
                            }
                        } catch (Exception e) {
                            System.out.println("Error: Please enter a valid integer.");
                        }
                    }
                }

                if (nxtBotPos > 25) {
                    System.out.println("Bot rolled " + b_roll + " but it is too high. Staying at " + b_loc);
                } else {
                    System.out.println("Bot rolled " + b_roll + " and moved to " + nxtBotPos);
                    
                    doHardwareMove(swiftBotInst, b_loc, nxtBotPos);
                    b_loc = nxtBotPos;

                    if (snakesMap.containsKey(b_loc)) {
                        int sDest = snakesMap.get(b_loc);
                        System.out.println("Bot hit a snake. Sliding down to " + sDest);
                        doHardwareMove(swiftBotInst, b_loc, sDest);
                        b_loc = sDest;
                    } else if (LADDERS.containsKey(b_loc)) {
                        int lDest = LADDERS.get(b_loc);
                        System.out.println("Bot hit a ladder. Climbing up to " + lDest);
                        doHardwareMove(swiftBotInst, b_loc, lDest);
                        b_loc = lDest;
                    }
                }

                if (b_loc == 25) {
                    System.out.println("\n*** SWIFTBOT WINS! ***");
                    stillPlayingFlag = false;
                }
            }

            if (stillPlayingFlag == true) {
                if (p_loc == 5 || b_loc == 5) { // midpoint check
                    System.out.println("\nSquare 5 reached. Press X to abort the game, or Y to continue.");
                    String btnPressed = waitForXorY(swiftBotInst);
                    if (btnPressed.equals("X")) {
                        System.out.println("Game aborted by user.");
                        stillPlayingFlag = false;
                    }
                }
            }

            // toggle turn
            if (p_turn == true) {
                p_turn = false;
            } else {
                p_turn = true;
            }
        }

        System.out.println("\nGame Over. Press X to exit and save the log file.");
        waitForButton(swiftBotInst, Button.X);

        // format the time for the file name
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");
        String tStamp = LocalDateTime.now().format(fmt);
        String FNAME = "SnakesAndLadders_Log_" + tStamp + ".txt";

        try {
            FileWriter writerObj = new FileWriter(FNAME);
            writerObj.write("--- Game Log ---\n");
            writerObj.write("Played at: " + tStamp + "\n");
            writerObj.write("User final position: " + p_loc + "\n");
            writerObj.write("Bot final position: " + b_loc + "\n");
            writerObj.close();
            
            File f = new File(FNAME);
            System.out.println("Log saved to: " + f.getAbsolutePath());
        } catch (Exception io_err) {
            System.out.println("Error saving the log file.");
        }
    }

    // Handles the physical movement across the 5x5 grid
    private void doHardwareMove(SwiftBotAPI botAPI, int startSquare, int endSquare) {
        if (startSquare == endSquare) { return; } // dont do anything

        // calculate coordinates (y is row, x is col)
        int r1 = (startSquare - 1) / 5;
        int c1 = (startSquare - 1) % 5;
        if (r1 % 2 != 0) { c1 = 4 - c1; } // reverse for odd rows (snake pattern)
        
        int r2 = (endSquare - 1) / 5;
        int c2 = (endSquare - 1) % 5;
        if (r2 % 2 != 0) { c2 = 4 - c2; }

        // squares are 25cm
        double dX = (c2 - c1) * 25.0;
        double dY = (r2 - r1) * 25.0;
        
        double d_sq = (dX * dX) + (dY * dY);
        double pythag_dist = Math.sqrt(d_sq);
        
        // find angle to turn
        double t_angle = Math.toDegrees(Math.atan2(dY, dX));
        double amountToTurn = t_angle - currentAngle;

        // normalization so bot doesn't spin around fully
        if (amountToTurn > 180) {
            amountToTurn = amountToTurn - 360;
        } else if (amountToTurn <= -180) {
            amountToTurn = amountToTurn + 360;
        }

        // CALIBRATION CONSTANTS - change if bot under/over shoots!
        double ms_deg = 8.5;
        double cm_sec = 15.0; 

        int t_time_ms = (int)(Math.abs(amountToTurn) * ms_deg);
        int m_time_ms = (int)((pythag_dist / cm_sec) * 1000); 
        
        try {
            if (amountToTurn > 0) {
                botAPI.move(0, 50, t_time_ms); // right
            } else if (amountToTurn < 0) {
                botAPI.move(50, 0, t_time_ms); // left
            }
            
            currentAngle = t_angle; // update global heading
            Thread.sleep(400); // pause so motors dont overlap
            
            botAPI.move(50, 50, m_time_ms); // forwards
        } catch (Exception hw_e) {
            System.out.println("hardware err: " + hw_e.getMessage());
        }
    }
}