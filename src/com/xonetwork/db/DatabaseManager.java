package com.xonetwork.db;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:xonetwork.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE," +
                    "wins INTEGER DEFAULT 0," +
                    "losses INTEGER DEFAULT 0," +
                    "draws INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS matches (" +
                    "match_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player1 TEXT," +
                    "player2 TEXT," +
                    "winner TEXT," +
                    "date_time TEXT)");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void updatePlayerStats(String name, String result) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if player exists
            String selectSql = "SELECT id FROM players WHERE name = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, name);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    String insertSql = "INSERT INTO players (name) VALUES (?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                        insertPstmt.setString(1, name);
                        insertPstmt.executeUpdate();
                    }
                }
            }

            String updateSql;
            if ("WIN".equalsIgnoreCase(result)) {
                updateSql = "UPDATE players SET wins = wins + 1 WHERE name = ?";
            } else if ("LOSS".equalsIgnoreCase(result)) {
                updateSql = "UPDATE players SET losses = losses + 1 WHERE name = ?";
            } else {
                updateSql = "UPDATE players SET draws = draws + 1 WHERE name = ?";
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error updating player stats: " + e.getMessage());
        }
    }

    public static void recordMatch(String p1, String p2, String winner) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO matches (player1, player2, winner, date_time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, p1);
                pstmt.setString(2, p2);
                pstmt.setString(3, winner);
                pstmt.setString(4, LocalDateTime.now().toString());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording match: " + e.getMessage());
        }
    }

    public static String getScoreboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- SCOREBOARD ---\n");
        sb.append(String.format("%-15s | %-5s | %-5s | %-5s\n", "Player", "Wins", "Loss", "Draw"));
        sb.append("------------------------------------------\n");
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM players ORDER BY wins DESC")) {
            
            while (rs.next()) {
                sb.append(String.format("%-15s | %-5d | %-5d | %-5d\n",
                        rs.getString("name"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("draws")));
            }
        } catch (SQLException e) {
            sb.append("Error fetching scoreboard: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
