package com.sudoku;

import com.sudoku.generator.PuzzleGenerator;
import com.sudoku.model.Difficulty;
import com.sudoku.model.SudokuBoard;
import com.sudoku.solver.BacktrackingSolver;
import com.sudoku.solver.BacktrackingSolver.SolveStep;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SudokuTest {

    // ── Solver tests ───────────────────────────────────────────────────────

    @Test
    void solver_solvesKnownPuzzle() {
        // A well-known hard puzzle
        int[] puzzle = {
            8,0,0, 0,0,0, 0,0,0,
            0,0,3, 6,0,0, 0,0,0,
            0,7,0, 0,9,0, 2,0,0,
            0,5,0, 0,0,7, 0,0,0,
            0,0,0, 0,4,5, 7,0,0,
            0,0,0, 1,0,0, 0,3,0,
            0,0,1, 0,0,0, 0,6,8,
            0,0,8, 5,0,0, 0,1,0,
            0,9,0, 0,0,0, 4,0,0
        };
        int[] grid = Arrays.copyOf(puzzle, 81);
        BacktrackingSolver solver = new BacktrackingSolver();
        assertTrue(solver.solve(grid));
        // Verify no zeroes remain
        for (int v : grid) assertNotEquals(0, v);
        // Verify validity
        assertValidSolution(grid);
    }

    @Test
    void solver_recordsSteps() {
        int[] puzzle = {
            5,3,0, 0,7,0, 0,0,0,
            6,0,0, 1,9,5, 0,0,0,
            0,9,8, 0,0,0, 0,6,0,
            8,0,0, 0,6,0, 0,0,3,
            4,0,0, 8,0,3, 0,0,1,
            7,0,0, 0,2,0, 0,0,6,
            0,6,0, 0,0,0, 2,8,0,
            0,0,0, 4,1,9, 0,0,5,
            0,0,0, 0,8,0, 0,7,9
        };
        int[] grid = Arrays.copyOf(puzzle, 81);
        BacktrackingSolver solver = new BacktrackingSolver();
        List<SolveStep> steps = solver.solveWithSteps(grid);
        assertFalse(steps.isEmpty());
        // Grid should now be solved
        for (int v : grid) assertNotEquals(0, v);
    }

    @Test
    void solver_countsUniqueSolution() {
        int[] puzzle = {
            5,3,0, 0,7,0, 0,0,0,
            6,0,0, 1,9,5, 0,0,0,
            0,9,8, 0,0,0, 0,6,0,
            8,0,0, 0,6,0, 0,0,3,
            4,0,0, 8,0,3, 0,0,1,
            7,0,0, 0,2,0, 0,0,6,
            0,6,0, 0,0,0, 2,8,0,
            0,0,0, 4,1,9, 0,0,5,
            0,0,0, 0,8,0, 0,7,9
        };
        int[] grid = Arrays.copyOf(puzzle, 81);
        BacktrackingSolver solver = new BacktrackingSolver();
        assertEquals(1, solver.countSolutions(grid, 2));
    }

    // ── Generator tests ────────────────────────────────────────────────────

    @Test
    void generator_producesUniqueSolution_easy() {
        assertUnique(Difficulty.EASY);
    }

    @Test
    void generator_producesUniqueSolution_medium() {
        assertUnique(Difficulty.MEDIUM);
    }

    @Test
    void generator_producesUniqueSolution_hard() {
        assertUnique(Difficulty.HARD);
    }

    @Test
    void generator_givensCountMatchesDifficulty() {
        for (Difficulty d : Difficulty.values()) {
            PuzzleGenerator gen = new PuzzleGenerator(42L);
            SudokuBoard board = gen.generate(d);
            int givens = 0;
            for (int i = 0; i < 81; i++) if (board.isGiven(i)) givens++;
            // Allow some slack — uniqueness check may leave a few extra
            assertTrue(givens >= d.getGivens(),
                    "Expected >= " + d.getGivens() + " givens for " + d + ", got " + givens);
        }
    }

    // ── Board model tests ──────────────────────────────────────────────────

    @Test
    void board_cannotEditGivenCells() {
        int[] puzzle   = new int[81]; puzzle[0] = 5;
        int[] solution = Arrays.copyOf(puzzle, 81);
        SudokuBoard board = new SudokuBoard(puzzle, solution);
        assertFalse(board.setUserValue(0, 3)); // cell 0 is a given
        assertEquals(5, board.getUserValue(0));
    }

    @Test
    void board_resetRestoresPuzzle() {
        int[] puzzle   = new int[81]; puzzle[10] = 7;
        int[] solution = Arrays.copyOf(puzzle, 81); solution[5] = 9;
        SudokuBoard board = new SudokuBoard(puzzle, solution);
        board.setUserValue(5, 9);
        assertEquals(9, board.getUserValue(5));
        board.reset();
        assertEquals(0, board.getUserValue(5));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void assertUnique(Difficulty d) {
        PuzzleGenerator gen = new PuzzleGenerator(99L);
        SudokuBoard board = gen.generate(d);
        BacktrackingSolver solver = new BacktrackingSolver();
        int count = solver.countSolutions(board.getPuzzleCopy(), 2);
        assertEquals(1, count, "Puzzle for " + d + " should have exactly 1 solution");
    }

    private void assertValidSolution(int[] grid) {
        // Check rows
        for (int r = 0; r < 9; r++) {
            boolean[] seen = new boolean[10];
            for (int c = 0; c < 9; c++) {
                int v = grid[r * 9 + c];
                assertFalse(seen[v], "Duplicate in row " + r);
                seen[v] = true;
            }
        }
        // Check columns
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) {
                int v = grid[r * 9 + c];
                assertFalse(seen[v], "Duplicate in col " + c);
                seen[v] = true;
            }
        }
        // Check boxes
        for (int br = 0; br < 3; br++) for (int bc = 0; bc < 3; bc++) {
            boolean[] seen = new boolean[10];
            for (int dr = 0; dr < 3; dr++) for (int dc = 0; dc < 3; dc++) {
                int v = grid[(br * 3 + dr) * 9 + (bc * 3 + dc)];
                assertFalse(seen[v], "Duplicate in box " + br + "," + bc);
                seen[v] = true;
            }
        }
    }
}
