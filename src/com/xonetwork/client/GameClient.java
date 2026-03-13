package com.xonetwork.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean myTurn = false;
    private boolean running = true;
    private char mySymbol;
    private String playerName;
    
    // UI State
    private String currentBoard = "";
    private String currentInfo = "Waiting for game to start...";
    private String currentScoreboard = "";
    private java.util.List<String> chatHistory = new java.util.ArrayList<>();

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";
    private static final String CLEAR = "\u001B[H\u001B[2J";

    public static void main(String[] args) {
        new GameClient().start();
    }

    public void start() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to X-O Network Arena!");
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your name: ");
            playerName = scanner.nextLine();
            out.println("NAME " + playerName);

            // Thread to listen for server messages
            Thread listenerThread = new Thread(this::listenToServer);
            listenerThread.start();

            // Main loop for user input
            while (running) {
                if (myTurn) {
                    System.out.println("\nYour turn! Enter move (e.g., 'A1', 'B2') or type '/chat <msg>' or '/exit':");
                    String input = scanner.nextLine().trim();
                    handleUserInput(input);
                } else {
                    // Even if not turn, allow chat
                    String input = scanner.nextLine();
                    if (input.startsWith("/chat ")) {
                        out.println("CHAT " + input.substring(6));
                    } else if (input.equalsIgnoreCase("/exit")) {
                        out.println("EXIT");
                        running = false;
                    } else {
                        System.out.println("Wait for your turn or use /chat <msg>.");
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Client Error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleUserInput(String input) {
        if (input.startsWith("/chat ")) {
            out.println("CHAT " + input.substring(6));
        } else if (input.equalsIgnoreCase("/exit")) {
            out.println("EXIT");
            running = false;
        } else if (input.toUpperCase().matches("[A-C][1-3]")) {
            char rowChar = Character.toUpperCase(input.charAt(0));
            int row = rowChar - 'A' + 1;
            int col = Character.getNumericValue(input.charAt(1));
            out.println("MOVE " + row + "," + col);
            myTurn = false;
        } else {
            System.out.println("Invalid input. Use format like 'A1' (A-C, 1-3) or '/chat <msg>'.");
        }
    }

    private void listenToServer() {
        try {
            String serverMsg;
            while (running && (serverMsg = in.readLine()) != null) {
                String[] parts = serverMsg.split(" ", 2);
                String cmd = parts[0];
                String data = parts.length > 1 ? parts[1] : "";

                switch (cmd) {
                    case "INIT":
                        String[] initData = data.split(" ");
                        mySymbol = initData[0].charAt(0);
                        System.out.println("Assigned Symbol: " + mySymbol);
                        break;
                    case "BOARD":
                        currentBoard = data.replace("\\n", "\n");
                        refreshUI();
                        break;
                    case "INFO":
                        currentInfo = data;
                        refreshUI();
                        break;
                    case "SCORE":
                        currentScoreboard = data.replace("\\n", "\n");
                        refreshUI();
                        break;
                    case "TURN":
                        myTurn = true;
                        refreshUI();
                        break;
                    case "WAIT":
                        myTurn = false;
                        refreshUI();
                        break;
                    case "CHAT":
                        chatHistory.add(data);
                        if (chatHistory.size() > 5) chatHistory.remove(0);
                        refreshUI();
                        break;
                    case "ERROR":
                        System.out.println(RED + "\n!! " + data + RESET);
                        myTurn = true;
                        break;
                    case "WIN":
                    case "LOSS":
                    case "DRAW":
                        System.out.println(CYAN + "\n*** " + data + " ***" + RESET);
                        System.out.println("Type 'REPLAY' to play again or '/exit' to quit.");
                        break;
                    case "START":
                        currentInfo = "Game Started!";
                        refreshUI();
                        break;
                }
            }
        } catch (IOException e) {
            if (running) System.err.println("Disconnected from server.");
        }
    }

    private void refreshUI() {
        System.out.print(CLEAR);
        System.out.flush();
        
        System.out.println(BOLD + CYAN + "=== X-O NETWORK ARENA ===" + RESET);
        System.out.println("Player: " + GREEN + playerName + RESET + " | Symbol: " + 
                (mySymbol == 'X' ? RED : BLUE) + mySymbol + RESET);
        
        // Colorize the board
        String coloredBoard = currentBoard
            .replace("[X]", RED + "[X]" + RESET)
            .replace("[O]", BLUE + "[O]" + RESET)
            .replace(" X ", RED + " X " + RESET)
            .replace(" O ", BLUE + " O " + RESET);
        
        System.out.println(coloredBoard);
        System.out.println(YELLOW + currentInfo + RESET);
        
        if (!currentScoreboard.isEmpty()) {
            System.out.println(currentScoreboard);
        }
        
        System.out.println("\n" + BOLD + "--- RECENT CHAT ---" + RESET);
        for (String chat : chatHistory) {
            System.out.println(chat);
        }
        
        if (myTurn) {
            System.out.print(BOLD + GREEN + "\nYOUR TURN > " + RESET);
        } else {
            System.out.print(BOLD + YELLOW + "\nWAITING FOR OPPONENT... > " + RESET);
        }
    }

    private void cleanup() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}
