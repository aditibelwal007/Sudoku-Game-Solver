package com.sudoku.ui;

import com.sudoku.model.Difficulty;
import com.sudoku.model.LeaderboardEntry;
import com.sudoku.model.SudokuBoard;
import com.sudoku.generator.PuzzleGenerator;
import com.sudoku.solver.BacktrackingSolver;
import com.sudoku.solver.BacktrackingSolver.SolveStep;
import com.sudoku.util.GameTimer;
import com.sudoku.util.LeaderboardManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * JavaFX entry point. Wires together the board view, game controller,
 * toolbar, side panel, and leaderboard dialog.
 */
public class SudokuApp extends Application {

    // ── State ──────────────────────────────────────────────────────────────
    private SudokuBoard board;
    private final PuzzleGenerator generator  = new PuzzleGenerator();
    private final BacktrackingSolver solver   = new BacktrackingSolver();
    private final LeaderboardManager leaderboard = new LeaderboardManager();
    private final GameTimer timer = new GameTimer();

    private Difficulty currentDiff = Difficulty.MEDIUM;
    private int hintsUsed = 0;
    private int errors    = 0;
    private boolean solving = false;

    // ── UI Components ──────────────────────────────────────────────────────
    private BoardView boardView;
    private Label timerLabel;
    private Label filledLabel;
    private Label hintLabel;
    private Label errorLabel;
    private Label statusLabel;
    private ComboBox<Difficulty> diffCombo;

    // ── Application lifecycle ──────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        stage.setTitle("AI Sudoku Solver");
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #f8f8f8;");

        root.setTop(buildToolbar());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        newGame();
    }

    @Override
    public void stop() {
        timer.stop();
    }

    // ── UI builders ────────────────────────────────────────────────────────

    private HBox buildToolbar() {
        Label title = new Label("Sudoku Solver");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));

        diffCombo = new ComboBox<>();
        diffCombo.getItems().addAll(Difficulty.values());
        diffCombo.setValue(currentDiff);
        diffCombo.setOnAction(e -> currentDiff = diffCombo.getValue());

        Button newBtn    = btn("New Puzzle",    "#2563eb", e -> newGame());
        Button solveBtn  = btn("Solve (AI)",    "#059669", e -> solveWithAI());
        Button hintBtn   = btn("Hint",          "#7c3aed", e -> giveHint());
        Button resetBtn  = btn("Reset",         "#dc2626", e -> resetBoard());
        Button lbBtn     = btn("Leaderboard",   "#374151", e -> showLeaderboard());

        HBox bar = new HBox(10, title, new Region(), diffCombo, newBtn, solveBtn, hintBtn, resetBtn, lbBtn);
        HBox.setHgrow(bar.getChildren().get(1), Priority.ALWAYS);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 12, 0));
        return bar;
    }

    private HBox buildCenter() {
        boardView = new BoardView(this::onCellSelected);

        VBox sidePanel = new VBox(12);
        sidePanel.setPadding(new Insets(0, 0, 0, 16));
        sidePanel.setMinWidth(200);

        // Stats
        timerLabel  = statLabel("0:00");
        filledLabel = statLabel("0 / 81");
        hintLabel   = statLabel("0");
        errorLabel  = statLabel("0");

        sidePanel.getChildren().addAll(
            statBox("Time",   timerLabel),
            statBox("Filled", filledLabel),
            statBox("Hints",  hintLabel),
            statBox("Errors", errorLabel),
            buildNumpad()
        );

        timer.secondsProperty().addListener((o, ov, nv) ->
                timerLabel.setText(GameTimer.format(nv.intValue())));

        HBox center = new HBox(0, boardView, sidePanel);
        center.setAlignment(Pos.TOP_LEFT);
        return center;
    }

    private HBox buildStatusBar() {
        statusLabel = new Label("Generate a new puzzle to start.");
        statusLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        HBox bar = new HBox(statusLabel);
        bar.setPadding(new Insets(10, 0, 0, 0));
        return bar;
    }

    private GridPane buildNumpad() {
        GridPane pad = new GridPane();
        pad.setHgap(6); pad.setVgap(6);
        for (int n = 1; n <= 9; n++) {
            final int num = n;
            Button b = new Button(String.valueOf(n));
            b.setPrefSize(54, 42);
            b.setFont(Font.font("System", FontWeight.BOLD, 16));
            b.setStyle("-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 6; -fx-background-radius: 6;");
            b.setOnAction(e -> boardView.enterNumber(num));
            pad.add(b, (n - 1) % 3, (n - 1) / 3);
        }
        Button erase = new Button("Erase");
        erase.setPrefWidth(170); erase.setPrefHeight(36);
        erase.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; -fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #dc2626;");
        erase.setOnAction(e -> boardView.enterNumber(0));
        GridPane.setColumnSpan(erase, 3);
        pad.add(erase, 0, 3);
        return pad;
    }

    // ── Game logic callbacks ───────────────────────────────────────────────

    /** Called by BoardView when a user places or erases a number. */
    public void onCellSelected(int idx, int value) {
        if (board == null || solving) return;
        int prev = board.getUserValue(idx);
        if (!board.setUserValue(idx, value)) return; // given cell

        if (value != 0 && value != board.getSolutionValue(idx)) {
            errors++;
            errorLabel.setText(String.valueOf(errors));
            setStatus("That doesn't fit — check row, column, and 3×3 box.");
        } else if (value != 0) {
            setStatus("Correct!");
        }

        boardView.refresh(board);
        updateStats();
        checkWin();
    }

    private void newGame() {
        timer.reset(); hintsUsed = 0; errors = 0; solving = false;
        hintLabel.setText("0"); errorLabel.setText("0");
        setStatus("Generating " + currentDiff.getLabel() + " puzzle…");

        new Thread(() -> {
            SudokuBoard generated = generator.generate(currentDiff);
            Platform.runLater(() -> {
                board = generated;
                boardView.setBoard(board);
                boardView.refresh(board);
                updateStats();
                timer.start();
                setStatus("Puzzle ready! Click a cell, then type a number.");
            });
        }).start();
    }

    private void solveWithAI() {
        if (board == null || solving) return;
        solving = true;
        timer.stop();

        int[] workGrid = board.getPuzzleCopy();
        List<SolveStep> steps = solver.solveWithSteps(workGrid);

        setStatus("AI solving with backtracking + MRV constraint propagation…");

        Thread animThread = new Thread(() -> {
            for (SolveStep step : steps) {
                try { Thread.sleep(step.backtrack ? 2 : 8); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> {
                    board.setUserValue(step.position, step.value);
                    boardView.refresh(board);
                    updateStats();
                });
            }
            Platform.runLater(() -> {
                solving = false;
                setStatus("AI solved the puzzle in " + steps.size() + " operations.");
                checkWin();
            });
        });
        animThread.setDaemon(true);
        animThread.start();
    }

    private void giveHint() {
        if (board == null || solving) return;
        for (int i = 0; i < 81; i++) {
            if (!board.isGiven(i) && board.getUserValue(i) != board.getSolutionValue(i)) {
                board.setUserValue(i, board.getSolutionValue(i));
                hintsUsed++;
                hintLabel.setText(String.valueOf(hintsUsed));
                boardView.markHint(i);
                boardView.refresh(board);
                updateStats();
                int row = i / 9 + 1, col = i % 9 + 1;
                setStatus("Hint: placed " + board.getSolutionValue(i) + " at row " + row + ", column " + col + ".");
                checkWin();
                return;
            }
        }
        setStatus("No more hints needed — check your entries.");
    }

    private void resetBoard() {
        if (board == null) return;
        timer.reset();
        errors = 0; hintsUsed = 0; solving = false;
        board.reset();
        boardView.refresh(board);
        updateStats();
        timer.start();
        setStatus("Board reset. Good luck!");
    }

    private void checkWin() {
        if (board != null && board.isSolved()) {
            timer.stop();
            solving = true;

            TextInputDialog dialog = new TextInputDialog("Player");
            dialog.setTitle("Puzzle Complete!");
            dialog.setHeaderText("🎉 You solved the " + currentDiff.getLabel() + " puzzle in " + timer.getFormatted() + "!");
            dialog.setContentText("Enter your name:");
            Optional<String> result = dialog.showAndWait();
            String name = result.orElse("Anonymous").trim();
            if (name.isEmpty()) name = "Anonymous";

            LeaderboardEntry entry = new LeaderboardEntry(name, currentDiff, timer.getSeconds(), hintsUsed, errors);
            leaderboard.addEntry(entry);
            setStatus("Solved! Score saved to leaderboard.");
        }
    }

    private void showLeaderboard() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Leaderboard");
        dialog.setHeaderText("Top scores across all difficulties");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(8);
        content.setPadding(new Insets(10));

        List<LeaderboardEntry> all = leaderboard.getAllEntries();
        if (all.isEmpty()) {
            content.getChildren().add(new Label("No scores yet. Finish a puzzle!"));
        } else {
            GridPane grid = new GridPane();
            grid.setHgap(16); grid.setVgap(4);
            grid.addRow(0, bold("#"), bold("Name"), bold("Difficulty"), bold("Time"), bold("Hints"), bold("Errors"));
            for (int i = 0; i < Math.min(10, all.size()); i++) {
                LeaderboardEntry e = all.get(i);
                grid.addRow(i + 1,
                    new Label(String.valueOf(i + 1)),
                    new Label(e.getPlayerName()),
                    new Label(e.getDifficulty().getLabel()),
                    new Label(e.getFormattedTime()),
                    new Label(String.valueOf(e.getHintsUsed())),
                    new Label(String.valueOf(e.getErrors())));
            }
            content.getChildren().add(grid);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void updateStats() {
        if (board == null) return;
        filledLabel.setText(board.filledCount() + " / 81");
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private static Button btn(String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                   "-fx-background-radius: 6; -fx-font-size: 13px; -fx-padding: 6 14;");
        b.setOnAction(handler);
        return b;
    }

    private static Label statLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 18));
        return l;
    }

    private static VBox statBox(String labelText, Label value) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        VBox box = new VBox(2, lbl, value);
        box.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                     "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;");
        return box;
    }

    private static Label bold(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        return l;
    }
}
