package com.sudoku.solver;

import java.util.*;

/**
 * AC-3 (Arc Consistency Algorithm 3) — Constraint Propagation.
 *
 * Reduces the candidate domains of empty cells before and during backtracking,
 * cutting the search space dramatically on harder puzzles.
 *
 * Use: call propagate() to prune domains; if it returns false the current
 * partial assignment is already inconsistent (prune this branch immediately).
 */
public class ConstraintPropagator {

    /** Compute initial candidate sets from a flat grid (0 = empty). */
    public static Set<Integer>[] buildDomains(int[] grid) {
        @SuppressWarnings("unchecked")
        Set<Integer>[] domains = new HashSet[81];
        for (int i = 0; i < 81; i++) {
            if (grid[i] != 0) {
                domains[i] = new HashSet<>(Set.of(grid[i]));
            } else {
                domains[i] = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
            }
        }
        return domains;
    }

    /**
     * Run AC-3 on the given domains.
     * Returns false if any domain becomes empty (inconsistency detected).
     */
    public static boolean propagate(Set<Integer>[] domains) {
        Queue<int[]> queue = new ArrayDeque<>();

        // Initialise with all arcs
        for (int i = 0; i < 81; i++)
            for (int j : peers(i))
                queue.add(new int[]{i, j});

        while (!queue.isEmpty()) {
            int[] arc = queue.poll();
            int xi = arc[0], xj = arc[1];
            if (revise(domains, xi, xj)) {
                if (domains[xi].isEmpty()) return false;
                for (int xk : peers(xi))
                    if (xk != xj) queue.add(new int[]{xk, xi});
            }
        }
        return true;
    }

    /** Remove values from domain[xi] that have no support in domain[xj]. */
    private static boolean revise(Set<Integer>[] domains, int xi, int xj) {
        boolean revised = false;
        Iterator<Integer> it = domains[xi].iterator();
        while (it.hasNext()) {
            int v = it.next();
            // Arc constraint: xi ≠ xj — so v is unsupported only if xj's
            // entire domain equals {v} (meaning xj must take v, blocking xi).
            if (domains[xj].size() == 1 && domains[xj].contains(v)) {
                it.remove();
                revised = true;
            }
        }
        return revised;
    }

    // ── Peer computation ──────────────────────────────────────────────────

    private static final int[][] PEERS_CACHE = new int[81][];

    static {
        for (int i = 0; i < 81; i++) PEERS_CACHE[i] = computePeers(i);
    }

    public static int[] peers(int idx) { return PEERS_CACHE[idx]; }

    private static int[] computePeers(int idx) {
        Set<Integer> peers = new LinkedHashSet<>();
        int r = idx / 9, c = idx % 9;
        for (int col = 0; col < 9; col++) if (col != c) peers.add(r * 9 + col);
        for (int row = 0; row < 9; row++) if (row != r) peers.add(row * 9 + c);
        int br = (r / 3) * 3, bc = (c / 3) * 3;
        for (int dr = 0; dr < 3; dr++)
            for (int dc = 0; dc < 3; dc++) {
                int p = (br + dr) * 9 + (bc + dc);
                if (p != idx) peers.add(p);
            }
        return peers.stream().mapToInt(Integer::intValue).toArray();
    }
}
