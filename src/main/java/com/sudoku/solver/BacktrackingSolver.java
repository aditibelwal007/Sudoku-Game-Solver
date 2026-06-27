package com.sudoku.solver;

import com.sudoku.model.SudokuBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * AI solver using Backtracking + Constraint Satisfaction (MRV heuristic).
 *
 * Algorithm:
 *  1. Find the empty cell with the fewest legal candidates (MRV = Minimum
 *     Remaining Values).
 *  2. Try each candidate, recursing until solved or stuck.
 *  3. Back-track when no candidates remain for a cell.
 *
 * The solver can also count solutions (up to a given limit), which the
 * generator uses to guarantee a unique solution.
 */
public class BacktrackingSolver {

    /** Solve in-place. Returns true if a solution was found. */
    public boolean solve(int[] grid) {
        int pos = selectMRV(grid);
        if (pos == -1) return true; // all filled → solved

        for (int num = 1; num <= 9; num++) {
            if (SudokuBoard.isValidPlacement(grid, pos, num)) {
                grid[pos] = num;
                if (solve(grid)) return true;
                grid[pos] = 0; // backtrack
            }
        }
        return false;
    }

    /**
     * Solve and record every (position, value) assignment so the UI can
     * animate the solving process step-by-step.
     */
    public List<SolveStep> solveWithSteps(int[] grid) {
        List<SolveStep> steps = new ArrayList<>();
        solveSteps(grid, steps);
        return steps;
    }

    private boolean solveSteps(int[] grid, List<SolveStep> steps) {
        int pos = selectMRV(grid);
        if (pos == -1) return true;

        for (int num = 1; num <= 9; num++) {
            if (SudokuBoard.isValidPlacement(grid, pos, num)) {
                grid[pos] = num;
                steps.add(new SolveStep(pos, num, false));
                if (solveSteps(grid, steps)) return true;
                grid[pos] = 0;
                steps.add(new SolveStep(pos, 0, true)); // backtrack marker
            }
        }
        return false;
    }

    /**
     * Count distinct solutions, stopping once `limit` is reached.
     * Used by the generator to verify uniqueness (limit = 2).
     */
    public int countSolutions(int[] grid, int limit) {
        int pos = grid[0] == -1 ? -1 : findFirstEmpty(grid);
        if (pos == -1) return 1;

        int count = 0;
        for (int num = 1; num <= 9; num++) {
            if (SudokuBoard.isValidPlacement(grid, pos, num)) {
                grid[pos] = num;
                count += countSolutions(grid, limit);
                grid[pos] = 0;
                if (count >= limit) return count;
            }
        }
        return count;
    }

    // ── MRV (Minimum Remaining Values) heuristic ──────────────────────────

    /**
     * Returns the index of the empty cell with the smallest candidate set,
     * or -1 if the grid is complete.
     */
    private int selectMRV(int[] grid) {
        int bestPos = -1, bestCount = 10;
        for (int i = 0; i < 81; i++) {
            if (grid[i] != 0) continue;
            int count = candidateCount(grid, i);
            if (count < bestCount) {
                bestCount = count;
                bestPos = i;
                if (count == 1) break; // can't do better
            }
        }
        return bestPos;
    }

    private int candidateCount(int[] grid, int pos) {
        int count = 0;
        for (int n = 1; n <= 9; n++)
            if (SudokuBoard.isValidPlacement(grid, pos, n)) count++;
        return count;
    }

    private int findFirstEmpty(int[] grid) {
        for (int i = 0; i < 81; i++) if (grid[i] == 0) return i;
        return -1;
    }

    // ── Step record ────────────────────────────────────────────────────────

    public static class SolveStep {
        public final int position;
        public final int value;       // 0 = erase (backtrack)
        public final boolean backtrack;

        public SolveStep(int position, int value, boolean backtrack) {
            this.position  = position;
            this.value     = value;
            this.backtrack = backtrack;
        }
    }
}
