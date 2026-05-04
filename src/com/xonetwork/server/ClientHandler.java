package com.xonetwork.server;

import com.xonetwork.common.GameLogic;
import com.xonetwork.db.DatabaseManager;
import java.io.*;
import java.net.Socket;

/**
 * ClientHandler manages the network communication for a single player.
 * It runs in its own thread to handle concurrent input from the user (moves, chat).
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private char symbol;
    private int playerNum;
    private String playerName;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, char symbol, int playerNum) {
        this.socket = socket;
        this.symbol = symbol;
        this.playerNum = playerNum;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendMessage("INIT " + symbol + " " + playerNum);
            
            String input;
            while ((input = in.readLine()) != null) {
                String[] parts = input.split(" ", 2);
                String command = parts[0];

                switch (command) {
                    case "NAME":
                        this.playerName = parts[1];
                        System.out.println("Player " + playerNum + " identified as: " + playerName);
                        break;
                    case "MOVE":
                        handleMoveCommand(parts[1]);
                        break;
                    case "CHAT":
                        GameServer.broadcastChat(playerName != null ? playerName : "Player " + playerNum, parts[1]);
                        break;
                    case "HISTORY":
                        sendMessage("CHAT [HISTORY]:\\n" + GameServer.getHistory().replace("\n", "\\n"));
                        break;
                    case "REPLAY":
                        handleReplay();
                        break;
                    case "./EXIT":
                    case "EXIT":
                        return;
                }
            }
        } catch (IOException e) {
            System.err.println("Client handler IO error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in ClientHandler for Player " + playerNum + ":");
            e.printStackTrace();
        } finally {
            GameServer.handlePlayerDisconnect(this);
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void handleMoveCommand(String moveData) {
        try {
            String[] coords = moveData.split(",");
            int row = Integer.parseInt(coords[0]) - 1;
            int col = Integer.parseInt(coords[1]) - 1;
            
            GameServer.handleMove(row, col, symbol);
        } catch (Exception e) {
            sendMessage("ERROR Invalid input format. Use row,col (e.g. 1,1)");
        }
    }

    public void updatePersistence(String result) {
        String p1Name = GameServer.getClients().get(0).playerName;
        String p2Name = GameServer.getClients().get(1).playerName;
        
        if ("WIN".equals(result)) {
            DatabaseManager.updatePlayerStats(playerName, "WIN");
            // Find opponent
            for(ClientHandler c : GameServer.getClients()) {
                if(c != this) DatabaseManager.updatePlayerStats(c.playerName, "LOSS");
            }
            DatabaseManager.recordMatch(p1Name, p2Name, playerName);
        } else {
            DatabaseManager.updatePlayerStats(p1Name, "DRAW");
            DatabaseManager.updatePlayerStats(p2Name, "DRAW");
            DatabaseManager.recordMatch(p1Name, p2Name, "DRAW");
        }
        
        GameServer.broadcast("SCORE " + DatabaseManager.getScoreboard());
        logMove("Match ended: " + result + " by " + playerName);
    }

    private void handleReplay() {
        GameServer.handleReplayRequest(this);
    }

    public String getPlayerName() { return playerName; }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    private void logMove(String log) {
        try (PrintWriter logWriter = new PrintWriter(new FileWriter("match_history.log", true))) {
            logWriter.println(log);
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }

    public char getSymbol() { return symbol; }
    public int getPlayerNum() { return playerNum; }
}
