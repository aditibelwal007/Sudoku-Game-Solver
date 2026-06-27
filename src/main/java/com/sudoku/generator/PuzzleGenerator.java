package com.sudoku.generator;

import com.sudoku.model.Difficulty;
import com.sudoku.model.SudokuBoard;
import com.sudoku.solver.BacktrackingSolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates valid Sudoku puzzles with a guaranteed unique solution.
 *
 * Steps:
 *  1. Fill a blank grid via randomised backtracking → full valid solution.
 *  2. Remove clues one at a time (random order), checking after each removal
 *     that exactly one solution still exists.
 *  3. Stop once the target number of givens for the chosen difficulty is reached.
 */
public class PuzzleGenerator {

    private final BacktrackingSolver solver = new BacktrackingSolver();
    private final Random rng;

    public PuzzleGenerator() { this(new Random()); }
    public PuzzleGenerator(long seed) { this(new Random(seed)); }
    private PuzzleGenerator(Random rng) { this.rng = rng; }

    /**
     * Generate a puzzle at the given difficulty.
     * @return a fully initialised SudokuBoard with puzzle + solution set.
     */
    public SudokuBoard generate(Difficulty difficulty) {
        int[] solution = generateFullGrid();
        int[] puzzle   = createPuzzle(solution, difficulty.getGivens());
        return new SudokuBoard(puzzle, solution);
    }

    // ── Full grid generation ───────────────────────────────────────────────

    private int[] generateFullGrid() {
        int[] grid = new int[81];
        fillGrid(grid);
        return grid;
    }

    private boolean fillGrid(int[] grid) {
        int pos = -1;
        for (int i = 0; i < 81; i++) { if (grid[i] == 0) { pos = i; break; } }
        if (pos == -1) return true; // complete

        List<Integer> nums = new ArrayList<>(List.of(1,2,3,4,5,6,7,8,9));
        Collections.shuffle(nums, rng);

        for (int n : nums) {
            if (SudokuBoard.isValidPlacement(grid, pos, n)) {
                grid[pos] = n;
                if (fillGrid(grid)) return true;
                grid[pos] = 0;
            }
        }
        return false;
    }

    // ── Puzzle creation (clue removal) ────────────────────────────────────

    private int[] createPuzzle(int[] solution, int targetGivens) {
        int[] puzzle = Arrays.copyOf(solution, 81);

        // Shuffle removal order
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 81; i++) indices.add(i);
        Collections.shuffle(indices, rng);

        int givens = 81;
        for (int idx : indices) {
            if (givens <= targetGivens) break;

            int backup = puzzle[idx];
            puzzle[idx] = 0;

            // Check uniqueness
            int[] test = Arrays.copyOf(puzzle, 81);
            if (solver.countSolutions(test, 2) != 1) {
                puzzle[idx] = backup; // restore — removing this breaks uniqueness
            } else {
                givens--;
            }
        }
        return puzzle;
    }
}
