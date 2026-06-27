package com.sudoku.util;

import com.sudoku.model.Difficulty;
import com.sudoku.model.LeaderboardEntry;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the top-10 leaderboard per difficulty.
 * Persists to ~/.sudoku/leaderboard.dat via Java serialisation.
 */
public class LeaderboardManager {

    private static final int MAX_ENTRIES_PER_DIFF = 10;
    private static final Path SAVE_DIR  = Path.of(System.getProperty("user.home"), ".sudoku");
    private static final Path SAVE_FILE = SAVE_DIR.resolve("leaderboard.dat");

    private final Map<Difficulty, List<LeaderboardEntry>> board = new EnumMap<>(Difficulty.class);

    public LeaderboardManager() {
        for (Difficulty d : Difficulty.values()) board.put(d, new ArrayList<>());
        load();
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public void addEntry(LeaderboardEntry entry) {
        List<LeaderboardEntry> list = board.get(entry.getDifficulty());
        list.add(entry);
        list.sort(Comparator.naturalOrder());
        if (list.size() > MAX_ENTRIES_PER_DIFF) list.subList(MAX_ENTRIES_PER_DIFF, list.size()).clear();
        save();
    }

    /** Returns a read-only snapshot of entries for the given difficulty. */
    public List<LeaderboardEntry> getEntries(Difficulty difficulty) {
        return Collections.unmodifiableList(board.get(difficulty));
    }

    /** All entries across every difficulty, sorted by score. */
    public List<LeaderboardEntry> getAllEntries() {
        return board.values().stream()
                .flatMap(Collection::stream)
                .sorted()
                .collect(Collectors.toList());
    }

    public void clearAll() {
        board.values().forEach(List::clear);
        save();
    }

    // ── Persistence ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void load() {
        if (!Files.exists(SAVE_FILE)) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE.toFile()))) {
            Map<Difficulty, List<LeaderboardEntry>> loaded =
                    (Map<Difficulty, List<LeaderboardEntry>>) in.readObject();
            loaded.forEach((k, v) -> board.getOrDefault(k, new ArrayList<>()).addAll(v));
        } catch (Exception e) {
            System.err.println("Could not load leaderboard: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(SAVE_DIR);
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE.toFile()))) {
                out.writeObject(board);
            }
        } catch (IOException e) {
            System.err.println("Could not save leaderboard: " + e.getMessage());
        }
    }
}
