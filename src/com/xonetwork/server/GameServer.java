package com.xonetwork.server;

import com.xonetwork.common.GameBoard;
import com.xonetwork.db.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameServer manages the lifecycle of the Tic Tac Toe match.
 * It coordinates two client connections, manages the board state,
 * and broadcasts updates to players.
 */
public class GameServer {
    private static final int PORT = 8888;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static GameBoard board = new GameBoard();
    private static int currentPlayerIndex = 0; // 0 for Player 1 (X), 1 for Player 2 (O)
    private static boolean gameActive = false;
    private static java.util.Set<ClientHandler> replayRequests = new java.util.HashSet<>();
    private static java.util.List<String> matchHistory = new java.util.ArrayList<>();

    public static void main(String[] args) {
        System.out.println("X-O Network Arena Server starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);

            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                char symbol = (clients.isEmpty()) ? 'X' : 'O';
                ClientHandler handler = new ClientHandler(socket, symbol, clients.size() + 1);
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("Player " + handler.getPlayerNum() + " connected with symbol " + symbol);
                
                if (clients.size() == 1) {
                    handler.sendMessage("WAIT Waiting for opponent...");
                }
            }

            startGame();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static synchronized void startGame() {
        gameActive = true;
        board.resetBoard();
        replayRequests.clear();
        matchHistory.clear();
        matchHistory.add("--- Game Started ---");
        // Rotate starting player for variety on replay
        currentPlayerIndex = (clients.size() == 2) ? (currentPlayerIndex + 1) % 2 : 0;
        broadcast("START Game started!");
        broadcastBoard();
        notifyTurn();
    }

    public static synchronized void handleMove(int row, int col, char symbol) {
        if (!gameActive) return;
        
        char expectedSymbol = (currentPlayerIndex == 0) ? 'X' : 'O';
        if (symbol != expectedSymbol) {
            clients.get(currentPlayerIndex).sendMessage("ERROR Not your turn!");
            return;
        }

        if (board.setMove(row, col, symbol)) {
            String moveInfo = board.getLastMoveInfo();
            matchHistory.add(moveInfo);
            broadcastBoard();
            
            if (com.xonetwork.common.GameLogic.checkWin(board, symbol)) {
                gameActive = false;
                matchHistory.add("Game Over: " + symbol + " Wins!");
                broadcast("INFO " + moveInfo);
                broadcast("WIN " + symbol + " Wins!");
                clients.get(currentPlayerIndex).updatePersistence("WIN");
                broadcastEndOptions();
            } else if (com.xonetwork.common.GameLogic.checkDraw(board)) {
                gameActive = false;
                matchHistory.add("Game Over: Draw");
                broadcast("INFO " + moveInfo);
                broadcast("DRAW It's a draw!");
                clients.get(currentPlayerIndex).updatePersistence("DRAW");
                broadcastEndOptions();
            } else {
                broadcast("INFO " + moveInfo);
                rotateTurn();
            }
        } else {
            clients.get(currentPlayerIndex).sendMessage("ERROR Invalid move!");
        }
    }

    private static void broadcastEndOptions() {
        broadcast("INFO Type 'REPLAY' for a rematch or '/history' to see moves.");
    }

    public static synchronized void handleReplayRequest(ClientHandler client) {
        if (gameActive) return;
        
        replayRequests.add(client);
        if (replayRequests.size() == 2) {
            startGame();
        } else {
            String requester = client.getPlayerName() != null ? client.getPlayerName() : "Player " + client.getPlayerNum();
            broadcast("CHAT [SYSTEM]: " + requester + " wants a rematch! Type 'REPLAY' to accept.");
        }
    }

    public static String getHistory() {
        return String.join("\n", matchHistory);
    }

    public static synchronized void rotateTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        notifyTurn();
    }

    public static synchronized void notifyTurn() {
        for (int i = 0; i < clients.size(); i++) {
            if (i == currentPlayerIndex) {
                clients.get(i).sendMessage("TURN Your turn (" + clients.get(i).getSymbol() + ")");
            } else {
                clients.get(i).sendMessage("WAIT Opponent's turn...");
            }
        }
    }

    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void broadcastBoard() {
        String boardStr = board.getFormattedBoard().replace("\n", "\\n");
        broadcast("BOARD " + boardStr);
    }

    public static void broadcastChat(String sender, String msg) {
        broadcast("CHAT [" + sender + "]: " + msg);
    }
    
    public static GameBoard getBoard() {
        return board;
    }

    public static boolean isGameActive() {
        return gameActive;
    }

    public static void setGameActive(boolean active) {
        gameActive = active;
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }
}
