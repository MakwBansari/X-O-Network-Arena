package com.xonetwork.db;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DatabaseManager now uses a local file-based storage to avoid issues with missing JDBC drivers.
 * It tracks player stats and match history in 'scoreboard.dat' and 'matches.log'.
 */
public class DatabaseManager {
    private static final String STATS_FILE = "scoreboard.dat";
    private static final String MATCH_LOG = "matches.log";
    private static Properties playerStats = new Properties();

    static {
        loadStats();
        System.out.println("File-based Database initialized successfully.");
    }

    private static synchronized void loadStats() {
        File file = new File(STATS_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                playerStats.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading stats: " + e.getMessage());
            }
        }
    }

    private static synchronized void saveStats() {
        try (FileOutputStream fos = new FileOutputStream(STATS_FILE)) {
            playerStats.store(fos, "X-O Network Arena Player Stats");
        } catch (IOException e) {
            System.err.println("Error saving stats: " + e.getMessage());
        }
    }

    public static synchronized void updatePlayerStats(String name, String result) {
        if (name == null || name.isEmpty()) return;
        
        String key = name.toLowerCase();
        String currentStats = playerStats.getProperty(key, "0,0,0"); // wins,losses,draws
        String[] parts = currentStats.split(",");
        int wins = Integer.parseInt(parts[0]);
        int losses = Integer.parseInt(parts[1]);
        int draws = Integer.parseInt(parts[2]);

        if ("WIN".equalsIgnoreCase(result)) {
            wins++;
        } else if ("LOSS".equalsIgnoreCase(result)) {
            losses++;
        } else {
            draws++;
        }

        playerStats.setProperty(key, wins + "," + losses + "," + draws);
        // Store display name case-sensitively in a separate key if needed, 
        // but for now we'll just use the provided name for the scoreboard.
        playerStats.setProperty(key + ".name", name); 
        
        saveStats();
    }

    public static synchronized void recordMatch(String p1, String p2, String winner) {
        try (PrintWriter out = new PrintWriter(new FileWriter(MATCH_LOG, true))) {
            String timestamp = LocalDateTime.now().toString();
            out.println(timestamp + " | " + p1 + " vs " + p2 + " | Winner: " + winner);
        } catch (IOException e) {
            System.err.println("Error logging match: " + e.getMessage());
        }
    }

    public static synchronized String getScoreboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- SCOREBOARD ---\n");
        sb.append(String.format("%-15s | %-5s | %-5s | %-5s\n", "Player", "Wins", "Loss", "Draw"));
        sb.append("------------------------------------------\n");

        List<PlayerScore> scores = new ArrayList<>();
        for (String key : playerStats.stringPropertyNames()) {
            if (key.endsWith(".name")) continue;
            
            String name = playerStats.getProperty(key + ".name", key);
            String stats = playerStats.getProperty(key);
            String[] parts = stats.split(",");
            scores.add(new PlayerScore(name, 
                Integer.parseInt(parts[0]), 
                Integer.parseInt(parts[1]), 
                Integer.parseInt(parts[2])));
        }

        // Sort by wins descending
        scores.sort((a, b) -> b.wins - a.wins);

        for (PlayerScore ps : scores) {
            sb.append(String.format("%-15s | %-5d | %-5d | %-5d\n",
                    ps.name, ps.wins, ps.losses, ps.draws));
        }
        
        if (scores.isEmpty()) {
            sb.append("No matches recorded yet.\n");
        }

        return sb.toString();
    }

    private static class PlayerScore {
        String name;
        int wins, losses, draws;
        PlayerScore(String name, int wins, int losses, int draws) {
            this.name = name; this.wins = wins; this.losses = losses; this.draws = draws;
        }
    }
}
