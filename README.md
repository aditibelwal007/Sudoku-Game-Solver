<<<<<<< HEAD
# AI Sudoku Solver

A fully-featured Sudoku game built with **Java 21** and **JavaFX 21**, backed
by a real AI solver using **Backtracking + Constraint Propagation (AC-3/MRV)**.

---

## Features

| Feature | Details |
|---|---|
| Puzzle generator | Randomised backtracking; unique-solution guarantee |
| AI solver | Backtracking + MRV heuristic + AC-3 constraint propagation |
| Animated solving | Watch the AI place and backtrack step-by-step |
| Hint system | Reveals one correct cell at a time |
| Difficulty levels | Easy · Medium · Hard · Expert |
| Timer | Counts up from 0:00; stops on completion |
| Leaderboard | Top-10 per difficulty, persisted to `~/.sudoku/leaderboard.dat` |
| Keyboard support | Arrow keys to navigate, 1–9 to fill, Backspace to erase |

---

## Project Structure

```
SudokuSolver/
├── pom.xml
└── src/main/java/com/sudoku/
    ├── Main.java                        # Entry point
    ├── model/
    │   ├── Difficulty.java              # Enum: EASY / MEDIUM / HARD / EXPERT
    │   ├── SudokuBoard.java             # Grid state + validation
    │   └── LeaderboardEntry.java        # Score record
    ├── solver/
    │   ├── BacktrackingSolver.java      # Backtracking + MRV heuristic
    │   └── ConstraintPropagator.java    # AC-3 arc-consistency
    ├── generator/
    │   └── PuzzleGenerator.java         # Randomised generation + uniqueness check
    ├── ui/
    │   ├── SudokuApp.java               # JavaFX Application, game controller
    │   └── BoardView.java               # 9×9 grid component
    └── util/
        ├── GameTimer.java               # JavaFX-bound countup timer
        └── LeaderboardManager.java      # Persistence via Java serialisation
```

---

## Requirements

- Java 21+
- Maven 3.8+

JavaFX is pulled in automatically by Maven — no manual installation needed.

---

## Build & Run

```bash
# Clone / unzip the project, then:
cd SudokuSolver

# Run directly (fastest during development)
mvn javafx:run

# Build a runnable fat JAR
mvn package
java -jar target/sudoku-solver-1.0.0.jar
```

---

## Run Tests

```bash
mvn test
```

Tests cover:
- Solver correctness on a known hard puzzle
- Step recording for animation
- Unique-solution verification for each difficulty
- Givens count per difficulty
- Board model (given-cell protection, reset)

---

## Algorithm Details

### Puzzle Generator
1. Fill an empty grid with randomised backtracking → full valid solution.
2. Shuffle cell indices, then remove cells one by one.
3. After each removal, count solutions (limit = 2). If the count drops below 1,
   restore the cell. Stop when the target givens count is reached.

### AI Solver — Backtracking + MRV
1. **Select** the empty cell with the fewest legal candidates
   (Minimum Remaining Values heuristic).
2. **Try** each candidate in order.
3. **Recurse** — if a dead end is reached, backtrack and try the next value.
4. **AC-3** (Arc Consistency Algorithm 3) prunes candidate domains between
   assignments, eliminating values that would violate arc constraints before
   the solver even tries them.

The combination of MRV + AC-3 dramatically reduces the search space on Hard
and Expert puzzles compared to naive backtracking.

---

## Leaderboard Scoring

```
score = time_seconds + (hints × 30) + (errors × 10)
```

Lower is better. Scores are ranked within each difficulty.
=======
# Sudoku-Game-Solver
A desktop Sudoku Solver application built with Java, JavaFX, and Maven. The application features an intuitive graphical interface where users can enter Sudoku puzzles, solve them instantly using the backtracking algorithm, clear the board, and validate solutions. Designed with clean architecture and efficient algorithm implementation.
>>>>>>> 05102a8b46a6f697fbbe7a5e7555b07aa6a49d22
