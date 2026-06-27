package com.sudoku.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LeaderboardEntry implements Comparable<LeaderboardEntry>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String playerName;
    private final Difficulty difficulty;
    private final int timeSeconds;
    private final int hintsUsed;
    private final int errors;
    private final LocalDateTime timestamp;

    public LeaderboardEntry(String playerName, Difficulty difficulty,
                            int timeSeconds, int hintsUsed, int errors) {
        this.playerName  = playerName;
        this.difficulty  = difficulty;
        this.timeSeconds = timeSeconds;
        this.hintsUsed   = hintsUsed;
        this.errors      = errors;
        this.timestamp   = LocalDateTime.now();
    }

    // Score: lower is better. Penalties for hints (+30s each) and errors (+10s each).
    public int getScore() {
        return timeSeconds + hintsUsed * 30 + errors * 10;
    }

    public String getFormattedTime() {
        int m = timeSeconds / 60, s = timeSeconds % 60;
        return String.format("%d:%02d", m, s);
    }

    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"));
    }

    public String getPlayerName()  { return playerName; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getTimeSeconds()    { return timeSeconds; }
    public int getHintsUsed()      { return hintsUsed; }
    public int getErrors()         { return errors; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(this.getScore(), other.getScore());
    }

    @Override
    public String toString() {
        return String.format("%-12s %-8s %s  hints:%d errors:%d",
                playerName, difficulty.getLabel(), getFormattedTime(), hintsUsed, errors);
    }
}
