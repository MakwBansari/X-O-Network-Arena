package com.xonetwork.common;

import java.io.Serializable;

public class GameBoard implements Serializable {
    private static final long serialVersionUID = 1L;
    private char[][] grid;
    private static final int SIZE = 3;
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    private char lastSymbol = ' ';

    public GameBoard() {
        grid = new char[SIZE][SIZE];
        resetBoard();
    }

    public void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = ' ';
            }
        }
        lastMoveRow = -1;
        lastMoveCol = -1;
        lastSymbol = ' ';
    }

    public boolean setMove(int row, int col, char symbol) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE && grid[row][col] == ' ') {
            grid[row][col] = symbol;
            lastMoveRow = row;
            lastMoveCol = col;
            lastSymbol = symbol;
            return true;
        }
        return false;
    }

    public char[][] getGrid() {
        return grid;
    }

    public String getFormattedBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n      1     2     3  \n");
        sb.append("   +-----+-----+-----+\n");
        char rowLabel = 'A';
        for (int i = 0; i < SIZE; i++) {
            sb.append(" ").append(rowLabel++).append(" |");
            for (int j = 0; j < SIZE; j++) {
                char symbol = grid[i][j];
                String displaySymbol = (symbol == ' ') ? " " : String.valueOf(symbol);
                
                if (i == lastMoveRow && j == lastMoveCol) {
                    sb.append("[").append(displaySymbol).append("]");
                } else {
                    sb.append("  ").append(displaySymbol).append("  ");
                }
                sb.append("|");
            }
            sb.append("\n   +-----+-----+-----+\n");
        }
        return sb.toString();
    }

    public String getLastMoveInfo() {
        if (lastMoveRow == -1) return "No moves made yet.";
        char row = (char) ('A' + lastMoveRow);
        int col = lastMoveCol + 1;
        return "Last Move: " + lastSymbol + " at " + row + col;
    }

    public boolean isFull() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] == ' ') return false;
            }
        }
        return true;
    }
}
