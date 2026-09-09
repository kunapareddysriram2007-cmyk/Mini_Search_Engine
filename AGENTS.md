# TextHack - Agent Instructions

## Project

This repository contains the TextHack Mini Search Engine for the Advanced DSA course.

The objective is to build a working text analytics/search engine using algorithms from the course syllabus.

## Main requirements

The engine must support:

1. Pattern searching
2. Fuzzy matching
3. Document similarity
4. Citation-flow analysis
5. Project scheduling demonstrations
6. Large-prime primality testing

## Algorithm syllabus

### Module 2 - String Algorithms
- Naive String Matching
- KMP
- Z Algorithm
- Rabin-Karp
- Aho-Corasick
- Suffix Arrays
- Kasai LCP

### Module 3 - Dynamic Programming
- Levenshtein Edit Distance
- Damerau-Levenshtein
- Weighted Edit Distance
- Needleman-Wunsch
- Smith-Waterman
- Bitmask DP
- Tree DP
- SOS DP

### Module 4 - Network Flow
- Ford-Fulkerson
- Edmonds-Karp
- Dinic
- Min-Cut
- Bipartite Matching
- Min-Cost Max-Flow at intuition level

### Module 5 - NP-Completeness and Approximation
- NP/NP-hard concepts
- Reductions
- Vertex Cover 2-approximation
- PTAS/FPTAS/APX concepts
- Parameterized complexity preview

### Module 6 - Randomized and Parallel Algorithms
- Randomized Quicksort
- Miller-Rabin
- Randomized Hashing
- Reservoir Sampling
- Parallel Reduce
- Parallel Scan
- Parallel Sorting concepts

## Critical constraint

java.util.* is forbidden inside the algorithm engine.

Do not use Java library implementations to replace algorithms that students are expected to implement manually.

Examples that should NOT be used inside the algorithm engine:
- Arrays.sort()
- Collections.sort()
- ArrayList
- HashMap
- HashSet
- PriorityQueue

Implement required data structures manually.

## Engineering rules

- Inspect existing code before modifying anything.
- Do not rewrite working code unnecessarily.
- Do not modify unrelated files.
- Maintain modular architecture.
- Add tests for every algorithm.
- Record time and space complexity.
- Preserve existing functionality.
- Ask before making major architectural changes.