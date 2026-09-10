package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import com.minisearchengine.core.ds.CustomQueue;

/**
 * Aho-Corasick Multi-Pattern String Matching Algorithm.
 *
 * ========================
 * CORE IDEA
 * ========================
 * Aho-Corasick preprocesses a set of patterns into an automaton built on
 * top of a TRIE, then scans the text in a single left-to-right pass.
 * It simultaneously matches all patterns in O(n + m_total + z) time,
 * where z is the number of matches found.
 *
 * The automaton has three components:
 *
 * 1. TRIE (goto function)
 *    A tree where each root-to-node path spells a prefix of some pattern.
 *    Stored as: int[][] children[node][char_index]  (size ALPHABET x numNodes).
 *    A node stores an output if it is the end of one or more patterns.
 *
 * 2. FAILURE LINKS (fail[])
 *    For a node u reached by reading string s, fail[u] is the node reached by
 *    reading the longest proper suffix of s that is also a prefix of some pattern.
 *    Built by a BFS over the trie in O(m_total * ALPHABET) time.
 *    During search, a mismatch follows the failure link instead of resetting.
 *
 * 3. OUTPUT LINKS (output[])
 *    Each node stores a list of patterns that END at that node.
 *    Output links propagate outputs from failure-link ancestors so that
 *    all patterns ending at the current text position are reported, not just
 *    the longest one.
 *
 * ========================
 * WORKFLOW
 * ========================
 * Phase 1 — Build trie: insert each pattern character by character.
 * Phase 2 — Build failure + output via BFS: root children get fail = root.
 *            For deeper nodes, follow parent's failure link to compute fail[child].
 * Phase 3 — Search: walk the automaton on text; at each node collect outputs.
 *
 * ========================
 * COMPLEXITY
 * ========================
 * Preprocessing:
 *   Time:  O(m_total * ALPHABET)  — trie + BFS failure links.
 *   Space: O(m_total * ALPHABET)  — children table.
 *
 * Searching:
 *   Time:  O(n + z)  — n text chars + z matches reported.
 *   Space: O(z)      — for result list.
 *
 * Constraints:
 *   Zero java.util.* imports.
 *   Trie nodes use plain int[][] children array.
 *   BFS uses CustomQueue<Integer>.
 *   Outputs stored in CustomArrayList<String>[].
 */
public class AhoCorasick implements MultiPatternMatcher {

    /**
     * Alphabet size: all printable ASCII characters (0-127).
     * Avoids limiting the matcher to only lowercase letters.
     */
    private static final int ALPHABET = 128;

    // Maximum nodes = total characters across all patterns + 1 (root)
    private int maxNodes;
    private int numNodes;

    private int[][] children;       // children[node][char] = next node (-1 if none)
    private int[] fail;             // failure link for each node
    @SuppressWarnings("unchecked")
    private CustomArrayList<String>[] output; // patterns ending at each node

    private final CustomArrayList<String> patterns;

    /**
     * Constructs the Aho-Corasick automaton for the given patterns.
     *
     * @param patterns List of patterns to match simultaneously.
     */
    public AhoCorasick(CustomArrayList<String> patterns) {
        this.patterns = patterns;
        buildAutomaton();
    }

    // =========================================================
    // Phase 1 — Trie Construction
    // =========================================================

    @SuppressWarnings("unchecked")
    private void buildAutomaton() {
        // Calculate max possible trie nodes
        maxNodes = 1; // root
        for (int i = 0; i < patterns.size(); i++) {
            maxNodes += patterns.get(i).length();
        }

        children = new int[maxNodes][ALPHABET];
        fail     = new int[maxNodes];
        output   = (CustomArrayList<String>[]) new CustomArrayList[maxNodes];

        // Initialise all nodes
        for (int i = 0; i < maxNodes; i++) {
            output[i] = new CustomArrayList<>();
            for (int c = 0; c < ALPHABET; c++) {
                children[i][c] = -1;
            }
        }

        numNodes = 1; // root is node 0

        // Insert each pattern into the trie
        for (int i = 0; i < patterns.size(); i++) {
            insertPattern(patterns.get(i));
        }

        // Build failure links and propagate outputs via BFS
        buildFailureLinks();
    }

    private void insertPattern(String pattern) {
        int curr = 0; // start at root
        for (int i = 0; i < pattern.length(); i++) {
            int c = pattern.charAt(i);
            if (c >= ALPHABET) continue; // skip out-of-range chars
            if (children[curr][c] == -1) {
                children[curr][c] = numNodes++;
            }
            curr = children[curr][c];
        }
        output[curr].add(pattern);
    }

    // =========================================================
    // Phase 2 — Failure Link & Output Construction (BFS)
    // =========================================================

    private void buildFailureLinks() {
        CustomQueue<Integer> queue = new CustomQueue<>();

        // Depth-1 nodes: failure link points to root (0)
        for (int c = 0; c < ALPHABET; c++) {
            int child = children[0][c];
            if (child != -1) {
                fail[child] = 0;
                queue.offer(child);
            } else {
                // If root has no child for c, redirect to root itself
                children[0][c] = 0;
            }
        }

        // BFS: compute failure links for deeper nodes
        while (!queue.isEmpty()) {
            int curr = queue.poll();

            for (int c = 0; c < ALPHABET; c++) {
                int child = children[curr][c];
                if (child == -1) {
                    // No trie edge: redirect through failure link (automaton completion)
                    children[curr][c] = children[fail[curr]][c];
                } else {
                    // Compute fail[child]
                    fail[child] = children[fail[curr]][c];

                    // Propagate output of fail[child] into child
                    CustomArrayList<String> failOutput = output[fail[child]];
                    for (int i = 0; i < failOutput.size(); i++) {
                        output[child].add(failOutput.get(i));
                    }

                    queue.offer(child);
                }
            }
        }
    }

    // =========================================================
    // Phase 3 — Search
    // =========================================================

    /**
     * Searches the text for all pattern occurrences simultaneously.
     *
     * @param text The text to scan.
     * @return CustomArrayList<Match> where each Match holds a 0-based start index
     *         and the pattern that was found.
     */
    @Override
    public CustomArrayList<Match> searchAll(String text) {
        CustomArrayList<Match> matches = new CustomArrayList<>();

        if (text == null || patterns.isEmpty()) {
            return matches;
        }

        int curr = 0; // current automaton state

        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            if (c >= ALPHABET) {
                // Out-of-range char: reset to root
                curr = 0;
                continue;
            }

            // Follow automaton transition (already handles failure internally)
            curr = children[curr][c];

            // Collect all patterns ending at this state
            CustomArrayList<String> out = output[curr];
            for (int j = 0; j < out.size(); j++) {
                String pat = out.get(j);
                int startIndex = i - pat.length() + 1;
                matches.add(new Match(startIndex, pat));
            }
        }

        return matches;
    }
}