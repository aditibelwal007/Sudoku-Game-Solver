package com.sudoku.model;

import java.util.Arrays;

/**
 * Core model: 9×9 grid stored as a flat int[81].
 * Indices: row r, col c  →  r*9 + c
 */
public class SudokuBoard {

    private final int[] puzzle;    // original clues (0 = empty)
    private final int[] solution;  // complete solved grid
    private final int[] userGrid;  // current user state
    private final boolean[] given; // true where clue was placed

    public SudokuBoard(int[] puzzle, int[] solution) {
        this.puzzle    = Arrays.copyOf(puzzle, 81);
        this.solution  = Arrays.copyOf(solution, 81);
        this.userGrid  = Arrays.copyOf(puzzle, 81);
        this.given     = new boolean[81];
        for (int i = 0; i < 81; i++) given[i] = (puzzle[i] != 0);
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public int getUserValue(int idx)     { return userGrid[idx]; }
    public int getSolutionValue(int idx) { return solution[idx]; }
    public int getPuzzleValue(int idx)   { return puzzle[idx]; }
    public boolean isGiven(int idx)      { return given[idx]; }

    public int getUserValue(int row, int col)     { return getUserValue(idx(row, col)); }
    public int getSolutionValue(int row, int col) { return getSolutionValue(idx(row, col)); }
    public boolean isGiven(int row, int col)      { return isGiven(idx(row, col)); }

    // ── Mutations ──────────────────────────────────────────────────────────

    /**
     * Place a value (0 = erase). Returns false if cell is a given clue.
     */
    public boolean setUserValue(int idx, int value) {
        if (given[idx]) return false;
        userGrid[idx] = value;
        return true;
    }

    public boolean setUserValue(int row, int col, int value) {
        return setUserValue(idx(row, col), value);
    }

    /** Reset all user entries back to the original puzzle. */
    public void reset() {
        System.arraycopy(puzzle, 0, userGrid, 0, 81);
    }

    /** Copy the full solution into the user grid (used by the AI solver UI). */
    public void applySolution() {
        System.arraycopy(solution, 0, userGrid, 0, 81);
    }

    // ── Validation ─────────────────────────────────────────────────────────

    /** True if the user's value at idx matches the solution. */
    public boolean isCorrect(int idx) {
        return userGrid[idx] != 0 && userGrid[idx] == solution[idx];
    }

    /** True if placing `value` at `idx` violates any Sudoku constraint in `grid`. */
    public static boolean isValidPlacement(int[] grid, int idx, int value) {
        int r = idx / 9, c = idx % 9;
        // Row
        for (int col = 0; col < 9; col++)
            if (col != c && grid[idx(r, col)] == value) return false;
        // Column
        for (int row = 0; row < 9; row++)
            if (row != r && grid[idx(row, c)] == value) return false;
        // Box
        int br = (r / 3) * 3, bc = (c / 3) * 3;
        for (int dr = 0; dr < 3; dr++)
            for (int dc = 0; dc < 3; dc++) {
                int p = idx(br + dr, bc + dc);
                if (p != idx && grid[p] == value) return false;
            }
        return true;
    }

    /** True when every cell matches the solution. */
    public boolean isSolved() {
        return Arrays.equals(userGrid, solution);
    }

    /** Number of non-zero cells in userGrid. */
    public int filledCount() {
        int count = 0;
        for (int v : userGrid) if (v != 0) count++;
        return count;
    }

    // ── Snapshots ─────────────────────────────────────────────────────────

    public int[] getUserGridCopy()  { return Arrays.copyOf(userGrid, 81); }
    public int[] getSolutionCopy()  { return Arrays.copyOf(solution, 81); }
    public int[] getPuzzleCopy()    { return Arrays.copyOf(puzzle, 81); }

    // ── Helpers ────────────────────────────────────────────────────────────

    public static int idx(int row, int col) { return row * 9 + col; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            if (r > 0 && r % 3 == 0) sb.append("------+-------+------\n");
            for (int c = 0; c < 9; c++) {
                if (c > 0 && c % 3 == 0) sb.append("| ");
                int v = userGrid[idx(r, c)];
                sb.append(v == 0 ? ". " : v + " ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
