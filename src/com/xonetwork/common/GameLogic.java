package com.xonetwork.common;

public class GameLogic {
    public static boolean checkWin(GameBoard board, char symbol) {
        char[][] grid = board.getGrid();

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if ((grid[i][0] == symbol && grid[i][1] == symbol && grid[i][2] == symbol) ||
                (grid[0][i] == symbol && grid[1][i] == symbol && grid[2][i] == symbol)) {
                return true;
            }
        }

        // Check diagonals
        if ((grid[0][0] == symbol && grid[1][1] == symbol && grid[2][2] == symbol) ||
            (grid[0][2] == symbol && grid[1][1] == symbol && grid[2][0] == symbol)) {
            return true;
        }

        return false;
    }

    public static boolean checkDraw(GameBoard board) {
        return board.isFull() && !checkWin(board, 'X') && !checkWin(board, 'O');
    }
}
