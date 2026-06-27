package com.sudoku.ui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import com.sudoku.model.SudokuBoard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The 9×9 Sudoku grid rendered as a JavaFX GridPane.
 *
 * Cell states and their colours:
 *   given     – dark gray text, light background
 *   correct   – blue text
 *   error     – red text, soft red background
 *   hint      – green text, soft green background
 *   selected  – blue border highlight
 *   peer      – same row/col/box as selected, soft blue tint
 */
public class BoardView extends GridPane {

    // ── Colours ────────────────────────────────────────────────────────────
    private static final String BG_NORMAL   = "-fx-background-color: white;";
    private static final String BG_GIVEN    = "-fx-background-color: #f3f4f6;";
    private static final String BG_PEER     = "-fx-background-color: #eff6ff;";
    private static final String BG_SELECTED = "-fx-background-color: #dbeafe;";
    private static final String BG_ERROR    = "-fx-background-color: #fef2f2;";
    private static final String BG_HINT     = "-fx-background-color: #f0fdf4;";

    private static final String COL_GIVEN   = "-fx-text-fill: #111827;";
    private static final String COL_USER    = "-fx-text-fill: #2563eb;";
    private static final String COL_ERROR   = "-fx-text-fill: #dc2626;";
    private static final String COL_HINT    = "-fx-text-fill: #16a34a;";

    private static final int CELL_SIZE = 54;

    // ── State ──────────────────────────────────────────────────────────────
    private SudokuBoard board;
    private final Label[][] cells = new Label[9][9];
    private int selectedIdx = -1;
    private final Set<Integer> hintCells = new HashSet<>();

    /** Consumer receives (cellIndex, value) when user enters a number. */
    private final BiConsumer<Integer, Integer> inputHandler;
    private int pendingNumber = 0; // last numpad press

    // ── Constructor ────────────────────────────────────────────────────────

    public BoardView(BiConsumer<Integer, Integer> inputHandler) {
        this.inputHandler = inputHandler;
        setFocusTraversable(true);
        buildGrid();
        setOnKeyPressed(e -> {
            String key = e.getCode().getName();
            if (key.length() == 1 && Character.isDigit(key.charAt(0))) {
                enterNumber(Integer.parseInt(key));
            } else if (e.getCode().name().startsWith("NUMPAD")) {
                String dig = e.getCode().getName().replace("Numpad ", "");
                if (!dig.isEmpty() && Character.isDigit(dig.charAt(0)))
                    enterNumber(Integer.parseInt(dig));
            } else switch (e.getCode()) {
                case BACK_SPACE, DELETE -> enterNumber(0);
                case UP    -> moveSelection(-9);
                case DOWN  -> moveSelection(9);
                case LEFT  -> moveSelection(-1);
                case RIGHT -> moveSelection(1);
                default    -> {}
            }
            e.consume();
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public void setBoard(SudokuBoard board) {
        this.board = board;
        selectedIdx = -1;
        hintCells.clear();
    }

    public void refresh(SudokuBoard board) {
        this.board = board;
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                updateCell(r, c);
    }

    public void markHint(int idx) { hintCells.add(idx); }

    public void enterNumber(int n) {
        pendingNumber = n;
        if (selectedIdx >= 0 && board != null)
            inputHandler.accept(selectedIdx, n);
    }

    // ── Grid builder ───────────────────────────────────────────────────────

    private void buildGrid() {
        setHgap(0); setVgap(0);
        setBorder(new Border(new BorderStroke(
                Color.web("#374151"), BorderStrokeStyle.SOLID,
                new CornerRadii(6), new BorderWidths(2))));

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Label cell = new Label();
                cell.setMinSize(CELL_SIZE, CELL_SIZE);
                cell.setMaxSize(CELL_SIZE, CELL_SIZE);
                cell.setAlignment(Pos.CENTER);
                cell.setFont(Font.font("System", FontWeight.BOLD, 20));
                cell.setStyle(BG_NORMAL + COL_GIVEN);
                cell.setBorder(cellBorder(r, c));
                final int row = r, col = c;
                cell.setOnMouseClicked(e -> selectCell(row * 9 + col));
                cells[r][c] = cell;
                add(cell, c, r);
            }
        }
    }

    private Border cellBorder(int r, int c) {
        double top    = (r % 3 == 0 && r != 0) ? 2 : 0.5;
        double left   = (c % 3 == 0 && c != 0) ? 2 : 0.5;
        double bottom = 0, right = 0;
        Color boxColor  = Color.web("#374151");
        Color hairColor = Color.web("#d1d5db");
        BorderWidths bw = new BorderWidths(top, right, bottom, left);
        BorderStroke bs = new BorderStroke(
                top  > 1 ? boxColor : hairColor,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                left > 1 ? boxColor : hairColor,
                BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE,
                BorderStrokeStyle.NONE, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, bw, Insets.EMPTY);
        return new Border(bs);
    }

    private void updateCell(int r, int c) {
        int idx = r * 9 + c;
        Label cell = cells[r][c];
        int val = board.getUserValue(idx);
        cell.setText(val == 0 ? "" : String.valueOf(val));
        applyStyle(cell, idx, r, c);
    }

    private void applyStyle(Label cell, int idx, int r, int c) {
        boolean isSelected = idx == selectedIdx;
        boolean isPeer     = isSelected(r, c) && idx != selectedIdx;
        boolean isHint     = hintCells.contains(idx);
        boolean isGiven    = board.isGiven(idx);
        boolean hasValue   = board.getUserValue(idx) != 0;
        boolean isCorrect  = hasValue && board.getUserValue(idx) == board.getSolutionValue(idx);
        boolean isError    = hasValue && !isCorrect && !isGiven;

        String bg, fg;
        if (isSelected)    { bg = BG_SELECTED; fg = isGiven ? COL_GIVEN : COL_USER; }
        else if (isHint)   { bg = BG_HINT;     fg = COL_HINT; }
        else if (isError)  { bg = BG_ERROR;     fg = COL_ERROR; }
        else if (isPeer)   { bg = BG_PEER;      fg = isGiven ? COL_GIVEN : COL_USER; }
        else if (isGiven)  { bg = BG_GIVEN;     fg = COL_GIVEN; }
        else               { bg = BG_NORMAL;    fg = isCorrect ? COL_USER : COL_GIVEN; }

        cell.setStyle(bg + fg);
    }

    private boolean isSelected(int r, int c) {
        if (selectedIdx < 0) return false;
        int sr = selectedIdx / 9, sc = selectedIdx % 9;
        return r == sr || c == sc
            || (r / 3 == sr / 3 && c / 3 == sc / 3);
    }

    // ── Selection ──────────────────────────────────────────────────────────

    private void selectCell(int idx) {
        selectedIdx = idx;
        refresh(board);
        requestFocus();
    }

    private void moveSelection(int delta) {
        if (selectedIdx < 0) { selectCell(0); return; }
        int next = selectedIdx + delta;
        if (next >= 0 && next < 81) selectCell(next);
    }
}
