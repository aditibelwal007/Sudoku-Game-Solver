package com.sudoku.model;

public enum Difficulty {
    EASY(46, "Easy"),
    MEDIUM(35, "Medium"),
    HARD(28, "Hard"),
    EXPERT(22, "Expert");

    private final int givens;
    private final String label;

    Difficulty(int givens, String label) {
        this.givens = givens;
        this.label = label;
    }

    public int getGivens() { return givens; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}
