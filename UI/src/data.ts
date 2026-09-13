import type { SearchResult, DocumentData } from "./types"

export const CORPUS_STATS = {
  totalDocuments: "10,248",
  avgSearchTime: "24 ms",
  algorithms: 5,
  indexCoverage: "98%",
}

export const SEARCH_SUGGESTIONS = [
  "Operating System",
  "Computer Networks",
  "Machine Learning",
  "Data Structures",
  "Artificial Intelligence",
]

export const MOCK_RESULTS: SearchResult[] = [
  {
    id: "os-001",
    title: "Operating System",
    path: "corpus/computer-science/operating-system",
    snippet:
      "An operating system (OS) is system software that manages computer hardware, software resources, and provides common services for computer programs. The operating system acts as an intermediary between programs and the computer hardware.",
    relevance: 94,
    algorithm: "KMP",
    matches: 142,
    size: "12.4 KB",
    docType: "Wikipedia",
    date: "2024-01-15",
  },
  {
    id: "pm-002",
    title: "Process Management",
    path: "corpus/computer-science/process-management",
    snippet:
      "Process management is an integral part of any modern operating system. The operating system must allocate resources to processes, enable processes to share and exchange information, protecting each process from interference by other processes.",
    relevance: 87,
    algorithm: "KMP",
    matches: 118,
    size: "8.7 KB",
    docType: "Article",
    date: "2024-02-03",
  },
  {
    id: "mm-003",
    title: "Memory Management",
    path: "corpus/computer-science/memory-management",
    snippet:
      "Memory management is the functionality of an operating system which handles or manages primary memory. It moves processes back and forth between main memory and disk during execution to optimize resource usage across concurrent programs.",
    relevance: 82,
    algorithm: "KMP",
    matches: 96,
    size: "9.2 KB",
    docType: "Article",
    date: "2024-01-28",
  },
  {
    id: "cpu-004",
    title: "CPU Scheduling Algorithms",
    path: "corpus/computer-science/cpu-scheduling",
    snippet:
      "CPU scheduling is a process that allows one process to use the CPU while another process is on hold due to unavailability of resources like I/O. The operating system scheduler determines the order in which processes execute.",
    relevance: 76,
    algorithm: "KMP",
    matches: 74,
    size: "7.1 KB",
    docType: "Reference",
    date: "2024-03-10",
  },
  {
    id: "fs-005",
    title: "File Systems and Storage",
    path: "corpus/computer-science/file-systems",
    snippet:
      "A file system is a method and data structure that the operating system uses to control how data is stored and retrieved. Without a file system, stored data would be one large body of unaddressable information.",
    relevance: 72,
    algorithm: "KMP",
    matches: 68,
    size: "11.3 KB",
    docType: "Wikipedia",
    date: "2024-02-19",
  },
  {
    id: "vm-006",
    title: "Virtual Memory and Paging",
    path: "corpus/computer-science/virtual-memory",
    snippet:
      "Virtual memory is a feature of an operating system that enables a computer to compensate for shortages of physical memory by temporarily transferring pages of data from random access memory to disk storage.",
    relevance: 68,
    algorithm: "Z Algorithm",
    matches: 61,
    size: "6.8 KB",
    docType: "Article",
    date: "2024-01-05",
  },
  {
    id: "ipc-007",
    title: "Inter-Process Communication",
    path: "corpus/computer-science/ipc",
    snippet:
      "Inter-process communication (IPC) is a mechanism that allows processes to communicate with each other and synchronize their actions. The operating system provides several IPC mechanisms including shared memory, message passing, and pipes.",
    relevance: 65,
    algorithm: "Z Algorithm",
    matches: 55,
    size: "5.4 KB",
    docType: "Reference",
    date: "2024-04-02",
  },
  {
    id: "dl-008",
    title: "Deadlock Detection and Prevention",
    path: "corpus/computer-science/deadlock",
    snippet:
      "Deadlock is a situation in operating systems where a set of processes are blocked because each process is holding a resource and waiting for another resource acquired by another process. The operating system must detect and recover from deadlocks.",
    relevance: 61,
    algorithm: "KMP",
    matches: 48,
    size: "7.9 KB",
    docType: "Article",
    date: "2024-03-25",
  },
  {
    id: "sc-009",
    title: "System Calls and APIs",
    path: "corpus/computer-science/system-calls",
    snippet:
      "A system call is the programmatic way in which a computer program requests a service from the kernel of the operating system. System calls provide an essential interface between a process and the operating system kernel.",
    relevance: 58,
    algorithm: "Z Algorithm",
    matches: 43,
    size: "4.2 KB",
    docType: "Reference",
    date: "2024-02-14",
  },
  {
    id: "boot-010",
    title: "Boot Process and Initialization",
    path: "corpus/computer-science/boot-process",
    snippet:
      "The boot process is what happens from the time you press the power button to the time the operating system is loaded and ready to use. The BIOS or UEFI firmware initializes hardware before passing control to the bootloader.",
    relevance: 54,
    algorithm: "KMP",
    matches: 37,
    size: "3.6 KB",
    docType: "Article",
    date: "2024-05-01",
  },
]

export const MOCK_DOCUMENTS: Record<string, DocumentData> = {
  "os-001": {
    id: "os-001",
    title: "Operating System",
    path: "corpus/computer-science/operating-system",
    size: "12.4 KB",
    matches: 142,
    relevance: 94,
    algorithm: "KMP",
    pattern: "operating system",
    index: "Positional Inverted Index",
    searchTime: 18,
    wordCount: 2847,
    date: "2024-01-15",
    overview:
      "An operating system (OS) is system software that manages computer hardware, software resources, and provides common services for computer programs. Time-sharing operating systems schedule tasks for efficient use of the system and may also include accounting software for cost allocation of processor time, mass storage, peripherals, and other resources.",
    sections: [
      {
        heading: "Definition and Purpose",
        content:
          "An operating system acts as an intermediary between users and computer hardware. The purpose of an operating system is to provide an environment in which a user can execute programs conveniently and efficiently. The operating system must keep track of all processes and allocate resources accordingly.",
      },
      {
        heading: "Core Components",
        content:
          "The operating system is primarily composed of the kernel, which handles core functions such as process management, memory management, device management, and system calls. The kernel operates at the lowest level of the operating system, interfacing directly with the hardware. Above the kernel sits the shell and other system utilities.",
      },
      {
        heading: "Types of Operating Systems",
        content:
          "Operating systems can be classified into several types: batch operating systems, time-sharing operating systems, distributed operating systems, network operating systems, and real-time operating systems. Modern desktop operating systems include Windows, macOS, and Linux.",
      },
      {
        heading: "Process Management",
        content:
          "The operating system must keep track of all processes and allocate processor time between them according to scheduling algorithms. Process management involves creating and deleting both user and system processes, suspending and resuming processes, and providing mechanisms for process synchronization.",
      },
    ],
  },
  "pm-002": {
    id: "pm-002",
    title: "Process Management",
    path: "corpus/computer-science/process-management",
    size: "8.7 KB",
    matches: 118,
    relevance: 87,
    algorithm: "KMP",
    pattern: "operating system",
    index: "Positional Inverted Index",
    searchTime: 14,
    wordCount: 1934,
    date: "2024-02-03",
    overview:
      "Process management is an integral part of any modern operating system. It involves creating, scheduling, and terminating processes while managing their resources.",
    sections: [
      {
        heading: "Process States",
        content:
          "A process can be in one of several states: new, ready, running, waiting, or terminated. The operating system kernel maintains a process control block (PCB) for each process, storing state information, program counter, CPU registers, and memory management information.",
      },
      {
        heading: "Scheduling Algorithms",
        content:
          "The operating system uses various scheduling algorithms including First-Come-First-Served (FCFS), Shortest Job First (SJF), Round Robin, and Priority Scheduling. Each algorithm has different performance characteristics in terms of throughput, turnaround time, and response time.",
      },
    ],
  },
}

export const ALGORITHMS = [
  {
    id: "kmp",
    name: "KMP",
    fullName: "Knuth-Morris-Pratt",
    purpose: "Linear-time exact pattern matching",
    complexity: "O(n + m)",
    spaceComplexity: "O(m)",
    description:
      "Preprocesses the pattern to build a failure function that avoids redundant character comparisons, achieving guaranteed linear time complexity even in the worst case.",
  },
  {
    id: "z-algo",
    name: "Z Algorithm",
    fullName: "Z Algorithm",
    purpose: "String matching via Z-array prefix computation",
    complexity: "O(n + m)",
    spaceComplexity: "O(n + m)",
    description:
      "Constructs a Z-array where each entry Z[i] is the length of the longest substring starting at position i that matches a prefix of the string.",
  },
  {
    id: "aho-corasick",
    name: "Aho-Corasick",
    fullName: "Aho-Corasick",
    purpose: "Simultaneous multi-pattern matching",
    complexity: "O(n + m + z)",
    spaceComplexity: "O(mk)",
    description:
      "Builds a finite-state automaton from a set of patterns allowing all patterns to be searched simultaneously in a single pass through the text.",
  },
  {
    id: "rabin-karp",
    name: "Rabin-Karp",
    fullName: "Rabin-Karp",
    purpose: "Rolling hash-based substring search",
    complexity: "O(n + m) avg",
    spaceComplexity: "O(1)",
    description:
      "Uses a rolling hash function to quickly identify candidate match positions, then verifies character by character to resolve hash collisions.",
  },
  {
    id: "edit-distance",
    name: "Edit Distance",
    fullName: "Levenshtein Distance",
    purpose: "Fuzzy matching via dynamic programming",
    complexity: "O(n × m)",
    spaceComplexity: "O(n × m)",
    description:
      "Computes the minimum number of single-character edits (insertions, deletions, substitutions) required to transform one string into another.",
  },
  {
    id: "inverted-index",
    name: "Inverted Index",
    fullName: "Positional Inverted Index",
    purpose: "Fast document retrieval and ranking",
    complexity: "O(1) lookup",
    spaceComplexity: "O(n × t)",
    description:
      "Maps each unique term to a list of documents containing it with positional data, enabling phrase queries, proximity scoring, and TF-IDF ranking.",
  },
]

export const PIPELINE_STEPS = [
  {
    step: 1,
    name: "Raw Corpus",
    description: "10,248 documents ingested from academic Wikipedia and curated reference sources",
  },
  {
    step: 2,
    name: "Text Normalization",
    description: "Lowercase conversion, Unicode normalization, and whitespace cleanup applied uniformly",
  },
  {
    step: 3,
    name: "Tokenization",
    description: "Documents split into individual terms and meaningful n-gram sequences",
  },
  {
    step: 4,
    name: "Stop Word Removal",
    description: "High-frequency, low-information words removed from the token stream",
  },
  {
    step: 5,
    name: "Stemming",
    description: "Words reduced to root forms using the Porter stemmer algorithm",
  },
  {
    step: 6,
    name: "Index Building",
    description: "Positional inverted index constructed with term frequency and document frequency data",
  },
  {
    step: 7,
    name: "Query Processing",
    description: "User query parsed, normalized, and decomposed into index lookup terms",
  },
  {
    step: 8,
    name: "Pattern Matching",
    description: "KMP / Z Algorithm / Aho-Corasick applied to retrieve candidate documents",
  },
  {
    step: 9,
    name: "Relevance Ranking",
    description: "TF-IDF scoring combined with match density and positional proximity",
  },
  {
    step: 10,
    name: "Search Results",
    description: "Ranked results returned with metadata, scores, and highlighted snippets",
  },
]
