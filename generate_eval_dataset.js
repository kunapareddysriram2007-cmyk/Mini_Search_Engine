const fs = require('fs');
const path = require('path');

const baseDir = path.resolve(__dirname, 'Mini_Search_Engine/src/main/resources/evaluation/corpus');

const docs = [
  // 1. Operating Systems
  {
    path: 'operating-systems/operating-system-architecture.txt',
    type: 'Wikipedia',
    date: '2024-02-01',
    category: 'operating-systems',
    language: 'English',
    title: 'Operating System Architecture',
    overview: 'An operating system (OS) manages computer hardware resources and provides common execution services for application software.',
    sections: [
      { heading: 'Kernel and User Modes', content: 'Modern operating system architectures enforce a dual-mode operation separating kernel mode from user mode. The kernel manages core hardware access, system calls, virtual memory translation, and CPU context switching while preventing user applications from corrupting critical system memory.' },
      { heading: 'Monolithic vs Microkernel Designs', content: 'In a monolithic operating system kernel like Linux, memory management, process scheduling, file systems, and device drivers all execute within a unified kernel address space. Conversely, a microkernel architecture delegates non-essential OS services to user-space daemons communicating via IPC.' }
    ]
  },
  {
    path: 'operating-systems/process-scheduling.txt',
    type: 'Article',
    date: '2024-02-05',
    category: 'operating-systems',
    language: 'English',
    title: 'Process Scheduling Algorithms',
    overview: 'Process scheduling is an essential operating system mechanism that determines which ready process is allocated CPU execution time.',
    sections: [
      { heading: 'Preemptive vs Non-Preemptive Scheduling', content: 'CPU scheduling algorithms can be classified into preemptive scheduling and non-preemptive scheduling. In preemptive scheduling, the operating system can interrupt a running process when a higher-priority task arrives or when a time quantum expires.' },
      { heading: 'Common Scheduling Strategies', content: 'Common process scheduling algorithms include First-Come First-Served (FCFS), Shortest Job First (SJF), Round Robin (RR) with configurable time slices, and Multilevel Feedback Queue (MLFQ) that dynamically adjusts priority based on CPU burst history.' }
    ]
  },
  {
    path: 'operating-systems/memory-management.txt',
    type: 'Wikipedia',
    date: '2024-02-10',
    category: 'operating-systems',
    language: 'English',
    title: 'Memory Management and Virtual Memory',
    overview: 'Memory management in operating systems provides abstraction over physical RAM through paging, segmentation, and virtual memory.',
    sections: [
      { heading: 'Virtual Address Spaces and Paging', content: 'The operating system divides virtual memory into fixed-size chunks called pages, mapped to physical memory page frames via page tables and the Memory Management Unit (MMU). The Translation Lookaside Buffer (TLB) caches recent virtual-to-physical address translations.' },
      { heading: 'Page Replacement Algorithms', content: 'When memory overcommit occurs and page faults happen, page replacement algorithms such as Least Recently Used (LRU), First-In First-Out (FIFO), and the Clock algorithm select candidate pages to evict to secondary swap storage.' }
    ]
  },
  {
    path: 'operating-systems/concurrency-and-deadlocks.txt',
    type: 'Guide',
    date: '2024-02-12',
    category: 'operating-systems',
    language: 'English',
    title: 'Concurrency, Synchronization and Deadlock Handling',
    overview: 'Concurrent processes sharing resources require synchronization primitives to prevent race conditions and deadlock states.',
    sections: [
      { heading: 'Synchronization Primitives', content: 'Mutexes, binary semaphores, counting semaphores, and condition variables coordinate thread execution across critical sections, ensuring mutual exclusion without race conditions.' },
      { heading: 'The Four Coffman Deadlock Conditions', content: 'A deadlock occurs when processes are permanently blocked because each holds resources while waiting for resources held by others. The four necessary conditions are Mutual Exclusion, Hold and Wait, No Preemption, and Circular Wait. The Bankers algorithm prevents deadlocks by evaluating safe states.' }
    ]
  },
  {
    path: 'operating-systems/filesystem-architecture.txt',
    type: 'Reference',
    date: '2024-02-15',
    category: 'operating-systems',
    language: 'English',
    title: 'File System Architecture and Inodes',
    overview: 'File systems structure persistent data storage across block devices using directory trees, inodes, and journaling.',
    sections: [
      { heading: 'Inodes and File Metadata', content: 'Unix-like file systems represent files via index nodes (inodes), which store file metadata including permissions, ownership, timestamps, and direct/indirect block pointers pointing to storage sectors.' },
      { heading: 'Journaling and Crash Consistency', content: 'Modern file systems like ext4, NTFS, and ZFS employ write-ahead journaling to guarantee crash consistency. Metadata updates are recorded to a sequential journal before committing to storage structures.' }
    ]
  },

  // 2. Algorithms
  {
    path: 'algorithms/graph-algorithms.txt',
    type: 'Wikipedia',
    date: '2024-02-18',
    category: 'algorithms',
    language: 'English',
    title: 'Graph Algorithms and Traversal',
    overview: 'Graph algorithms solve connectivity, traversal, shortest path, and network flow problems on vertices and edges.',
    sections: [
      { heading: 'Breadth-First and Depth-First Search', content: 'Breadth-First Search (BFS) uses a FIFO queue to explore graph levels in O(V + E) time, finding unweighted shortest paths. Depth-First Search (DFS) uses recursion or a stack to discover connected components, topological sorting, and cycle detection.' },
      { heading: 'Shortest Path and Spanning Tree', content: 'Dijkstra shortest path algorithm computes single-source shortest paths on non-negative weighted graphs using a priority queue in O((V + E) log V). Kruskal and Prim algorithms construct minimum spanning trees (MST).' }
    ]
  },
  {
    path: 'algorithms/dynamic-programming.txt',
    type: 'Article',
    date: '2024-02-20',
    category: 'algorithms',
    language: 'English',
    title: 'Dynamic Programming Principles and Patterns',
    overview: 'Dynamic programming solves complex optimization problems by breaking them down into overlapping subproblems with optimal substructure.',
    sections: [
      { heading: 'Memoization vs Tabulation', content: 'Top-down dynamic programming employs recursive memoization to cache intermediate results, while bottom-up tabulation iteratively fills a DP state table to eliminate call stack overhead.' },
      { heading: 'Classic DP Problems', content: 'Canonical dynamic programming problems include the 0/1 Knapsack problem, Longest Common Subsequence (LCS), Levenshtein edit distance, Matrix Chain Multiplication, and Floyd-Warshall all-pairs shortest paths.' }
    ]
  },
  {
    path: 'algorithms/sorting-algorithms.txt',
    type: 'Guide',
    date: '2024-02-22',
    category: 'algorithms',
    language: 'English',
    title: 'Comparison and Non-Comparison Sorting Algorithms',
    overview: 'Sorting algorithms arrange collections of elements in sequential order, evaluated by time and space complexity.',
    sections: [
      { heading: 'Divide and Conquer Sorts', content: 'Quicksort partitions arrays around a pivot element achieving O(n log n) average time, while Mergesort recursively divides the array into halves and merges sorted sub-arrays with guaranteed O(n log n) worst-case time.' },
      { heading: 'Linear-Time Non-Comparison Sorts', content: 'Counting Sort and Radix Sort bypass the theoretical O(n log n) comparison lower bound by exploiting integer key distribution to achieve O(n + k) linear running time.' }
    ]
  },
  {
    path: 'algorithms/string-matching-algorithms.txt',
    type: 'Wikipedia',
    date: '2024-02-25',
    category: 'algorithms',
    language: 'English',
    title: 'String Matching Algorithms: KMP, Aho-Corasick and Rabin-Karp',
    overview: 'String matching algorithms find occurrences of pattern strings within larger text corpora efficiently.',
    sections: [
      { heading: 'Knuth-Morris-Pratt and Z-Algorithm', content: 'The KMP string matcher precomputes an LPS failure table in O(m) time to skip redundant comparisons, searching text in linear O(n) time. The Z-algorithm computes a Z-array where each element measures the longest prefix match.' },
      { heading: 'Aho-Corasick and Rabin-Karp', content: 'Aho-Corasick constructs a trie finite-state automaton with failure links to match multiple patterns simultaneously in O(n + m + z) time. Rabin-Karp uses rolling hash functions for average linear-time substring search.' }
    ]
  },

  // 3. Data Structures
  {
    path: 'data-structures/b-trees-and-b-plus-trees.txt',
    type: 'Wikipedia',
    date: '2024-03-01',
    category: 'data-structures',
    language: 'English',
    title: 'B-Trees and B+ Trees in Storage Systems',
    overview: 'B-Trees and B+ Trees are self-balancing multi-way search trees optimized for block storage and database index structures.',
    sections: [
      { heading: 'Node Structure and High Fanout', content: 'Unlike binary search trees, a B-tree node holds multiple keys and child pointers. The high fanout keeps tree depth low (typically 3 to 4 levels for millions of records), minimizing disk I/O operations.' },
      { heading: 'B+ Tree Leaf Chain for Range Scans', content: 'In a B+ Tree, all record data pointers reside strictly in leaf nodes, which are linked together in a doubly linked list. This enables efficient sequential range queries without tree backtracking.' }
    ]
  },
  {
    path: 'data-structures/red-black-and-avl-trees.txt',
    type: 'Article',
    date: '2024-03-03',
    category: 'data-structures',
    language: 'English',
    title: 'Balanced Binary Search Trees: Red-Black and AVL',
    overview: 'Self-balancing binary search trees maintain O(log n) height guarantees through tree rotations during insert and delete.',
    sections: [
      { heading: 'AVL Tree Strict Balance', content: 'AVL trees strictly maintain a balance factor of at most 1 between left and right subtrees, providing faster lookup times at the expense of more frequent tree rotations during updates.' },
      { heading: 'Red-Black Tree Properties', content: 'Red-Black trees use node coloring rules (root is black, no consecutive red nodes, equal black-height) to guarantee that the longest path is at most twice the length of the shortest path.' }
    ]
  },
  {
    path: 'data-structures/hash-tables-and-collision-resolution.txt',
    type: 'Guide',
    date: '2024-03-05',
    category: 'data-structures',
    language: 'English',
    title: 'Hash Tables and Collision Resolution Strategies',
    overview: 'Hash tables provide average O(1) key-value insertion, deletion, and lookup operations using hash functions.',
    sections: [
      { heading: 'Hashing and Load Factors', content: 'A uniform hash function maps arbitrary keys to bucket indices. The load factor measures the ratio of stored elements to total bucket capacity, triggering table resizing when exceeding thresholds.' },
      { heading: 'Chaining vs Open Addressing', content: 'Collision resolution methods include separate chaining using linked lists or buckets, and open addressing strategies such as linear probing, quadratic probing, and double hashing.' }
    ]
  },
  {
    path: 'data-structures/heaps-and-priority-queues.txt',
    type: 'Reference',
    date: '2024-03-08',
    category: 'data-structures',
    language: 'English',
    title: 'Heaps and Priority Queue Implementations',
    overview: 'A heap is a specialized tree-based data structure that satisfies the heap property, powering priority queues.',
    sections: [
      { heading: 'Binary Heap Properties', content: 'In a min-heap, every parent node is less than or equal to its children, placing the minimum element at the root. Binary heaps are compactly represented inside contiguous arrays.' },
      { heading: 'Operations and Applications', content: 'Insertion and extract-min operate in O(log n) time, while heap building takes O(n). Priority queues built on heaps power CPU scheduling, Dijkstra shortest path, and Huffman coding.' }
    ]
  },
  {
    path: 'data-structures/trie-and-radix-trees.txt',
    type: 'Wikipedia',
    date: '2024-03-10',
    category: 'data-structures',
    language: 'English',
    title: 'Trie and Radix Tree Prefix Structures',
    overview: 'A trie is an ordered tree data structure used to store associative arrays where keys are usually sequences such as strings.',
    sections: [
      { heading: 'Prefix Matching and Autocomplete', content: 'Tries store shared prefixes along common branches, allowing prefix searching, autocomplete, and dictionary validation in O(L) time where L is string length, independent of total dictionary size.' },
      { heading: 'Radix Tree Compaction', content: 'A Radix tree (compact trie) compresses single-child node chains into single edges containing multi-character strings, reducing memory footprint while maintaining fast lookups.' }
    ]
  },

  // 4. Databases
  {
    path: 'databases/database-indexing.txt',
    type: 'Wikipedia',
    date: '2024-03-12',
    category: 'databases',
    language: 'English',
    title: 'Database Indexing Techniques and Query Performance',
    overview: 'Database indexing is a data structure technique to quickly locate and access data in a database without scanning every row.',
    sections: [
      { heading: 'Index Structures', content: 'Relational databases use clustered and non-clustered B+ tree indexes for range queries and equality lookups. Specialized engines also support hash indexes, GiST spatial indexes, and inverted indexes for full-text search.' },
      { heading: 'Query Planner and Optimization', content: 'The cost-based database query optimizer analyzes table statistics, index selectivity, and cardinality estimates to choose optimal execution plans between index scans, bitmap index scans, and sequential table scans.' }
    ]
  },
  {
    path: 'databases/relational-database-management-systems.txt',
    type: 'Article',
    date: '2024-03-15',
    category: 'databases',
    language: 'English',
    title: 'Relational Database Management Systems and SQL',
    overview: 'Relational database management systems (RDBMS) organize data into structured tables governed by relational algebra.',
    sections: [
      { heading: 'Relational Model and Constraints', content: 'RDBMS models enforce schema integrity through primary keys, foreign keys, unique constraints, and normal forms (1NF, 2NF, 3NF, BCNF) to eliminate redundant data anomaly.' },
      { heading: 'SQL Query Processing', content: 'Structured Query Language (SQL) provides declarative query syntax. The database parser constructs an abstract syntax tree and optimizes joins (nested loop join, hash join, merge join).' }
    ]
  },
  {
    path: 'databases/nosql-document-stores.txt',
    type: 'Guide',
    date: '2024-03-18',
    category: 'databases',
    language: 'English',
    title: 'NoSQL Databases and Distributed Document Stores',
    overview: 'NoSQL databases provide flexible schema designs, horizontal scaling, and specialized data models for web-scale workloads.',
    sections: [
      { heading: 'NoSQL Data Models', content: 'NoSQL systems encompass document stores (MongoDB, Couchbase), key-value databases (Redis, DynamoDB), wide-column stores (Cassandra), and graph databases (Neo4j).' },
      { heading: 'The CAP Theorem and Consistency', content: 'The CAP theorem states that a distributed data store can simultaneously provide at most two of Consistency, Availability, and Partition tolerance. Many NoSQL systems adopt eventual consistency.' }
    ]
  },
  {
    path: 'databases/acid-transactions-and-isolation.txt',
    type: 'Reference',
    date: '2024-03-20',
    category: 'databases',
    language: 'English',
    title: 'ACID Transactions and Concurrency Isolation Levels',
    overview: 'Database transactions guarantee data reliability and correctness across concurrent multi-user database operations.',
    sections: [
      { heading: 'ACID Guarantees', content: 'Atomicity ensures all-or-nothing execution; Consistency maintains database invariants; Isolation shields concurrent operations; Durability guarantees committed changes persist across system crashes using Write-Ahead Logging (WAL).' },
      { heading: 'SQL Isolation Levels', content: 'ANSI SQL defines four transaction isolation levels: Read Uncommitted, Read Committed, Repeatable Read, and Serializable, trading concurrency throughput against phenomena like dirty reads, non-repeatable reads, and phantom reads.' }
    ]
  },

  // 5. Computer Networks
  {
    path: 'computer-networks/tcp-ip-protocol-suite.txt',
    type: 'Wikipedia',
    date: '2024-03-22',
    category: 'computer-networks',
    language: 'English',
    title: 'TCP/IP Protocol Suite and Architecture',
    overview: 'The Internet protocol suite (TCP/IP) defines the networking protocols and communication architecture powering the global Internet.',
    sections: [
      { heading: 'Layered Architecture', content: 'The TCP/IP model comprises four layers: Link Layer (Ethernet, Wi-Fi), Internet Layer (IP, ICMP, ARP), Transport Layer (TCP, UDP), and Application Layer (HTTP, DNS, TLS, SSH).' },
      { heading: 'TCP Reliable Transport', content: 'Transmission Control Protocol (TCP) establishes connections via a three-way handshake (SYN, SYN-ACK, ACK), providing ordered byte-stream delivery, sliding window flow control, and congestion control algorithms (Reno, Cubic, BBR).' }
    ]
  },
  {
    path: 'computer-networks/network-routing-algorithms.txt',
    type: 'Article',
    date: '2024-03-25',
    category: 'computer-networks',
    language: 'English',
    title: 'Network Routing Protocols and Path Algorithms',
    overview: 'Network routing algorithms determine the optimal path for forwarding data packets across autonomous network domains.',
    sections: [
      { heading: 'Intra-Domain Routing: OSPF and RIP', content: 'Open Shortest Path First (OSPF) uses link-state advertisements and Dijkstra algorithm to calculate shortest paths. Routing Information Protocol (RIP) uses distance-vector algorithms and Bellman-Ford.' },
      { heading: 'Inter-Domain Routing with BGP', content: 'Border Gateway Protocol (BGP) is the path-vector routing protocol that manages routing decisions across Autonomous Systems (AS) across the global Internet backbone.' }
    ]
  },
  {
    path: 'computer-networks/domain-name-system.txt',
    type: 'Guide',
    date: '2024-03-28',
    category: 'computer-networks',
    language: 'English',
    title: 'Domain Name System Architecture and Resolution',
    overview: 'The Domain Name System (DNS) translates human-readable domain names into numerical IP addresses for network routing.',
    sections: [
      { heading: 'Hierarchical Namespace', content: 'DNS is structured hierarchically into root zone servers (13 named root authorities), Top-Level Domain (TLD) servers (.com, .org), authoritative name servers, and recursive resolvers.' },
      { heading: 'Record Types and Caching', content: 'Common DNS record types include A (IPv4), AAAA (IPv6), CNAME (canonical alias), MX (mail exchange), and TXT. Recursive resolvers cache query answers based on Time-To-Live (TTL) values.' }
    ]
  },

  // 6. Machine Learning
  {
    path: 'machine-learning/machine-learning-algorithms.txt',
    type: 'Wikipedia',
    date: '2024-04-01',
    category: 'machine-learning',
    language: 'English',
    title: 'Machine Learning Algorithms and Model Paradigms',
    overview: 'Machine learning is a field of artificial intelligence that builds mathematical models from sample data to make predictions.',
    sections: [
      { heading: 'Core Paradigms', content: 'Machine learning algorithms are categorized into supervised learning (labeled data), unsupervised learning (clustering, dimensionality reduction like PCA), and reinforcement learning (reward maximization in dynamic environments).' },
      { heading: 'Model Generalization and Evaluation', content: 'Training machine learning models requires balancing bias and variance to prevent overfitting. Common evaluation metrics include Precision, Recall, F1-score, ROC-AUC, and Mean Squared Error (MSE).' }
    ]
  },
  {
    path: 'machine-learning/gradient-descent-optimization.txt',
    type: 'Article',
    date: '2024-04-03',
    category: 'machine-learning',
    language: 'English',
    title: 'Gradient Descent and Optimization in Machine Learning',
    overview: 'Gradient descent is a first-order iterative optimization algorithm for finding a local minimum of a differentiable loss function.',
    sections: [
      { heading: 'Mechanics of Gradient Descent', content: 'The algorithm iteratively updates model parameters in the opposite direction of the gradient of the loss function with respect to parameters, scaled by a learning rate hyperparameter.' },
      { heading: 'Stochastic and Adaptive Variants', content: 'Stochastic Gradient Descent (SGD) computes gradients on random mini-batches. Advanced optimizers like Adam, RMSprop, and AdaGrad adjust learning rates adaptively per parameter.' }
    ]
  },
  {
    path: 'machine-learning/supervised-learning.txt',
    type: 'Guide',
    date: '2024-04-05',
    category: 'machine-learning',
    language: 'English',
    title: 'Supervised Learning: Classification and Regression',
    overview: 'Supervised learning algorithms infer mathematical mappings from labeled training input pairs to output target variables.',
    sections: [
      { heading: 'Classification Algorithms', content: 'Classification models predict discrete category labels. Key algorithms include Logistic Regression, Support Vector Machines (SVM) with kernel trick, k-Nearest Neighbors (k-NN), and Naive Bayes classifiers.' },
      { heading: 'Regression Models', content: 'Regression models predict continuous quantities using Ordinary Least Squares (OLS), Ridge regression (L2 regularization), Lasso regression (L1 regularization), and polynomial curve fitting.' }
    ]
  },

  // 7. Artificial Intelligence
  {
    path: 'artificial-intelligence/artificial-intelligence.txt',
    type: 'Wikipedia',
    date: '2024-04-08',
    category: 'artificial-intelligence',
    language: 'English',
    title: 'Artificial Intelligence and Intelligent Agents',
    overview: 'Artificial intelligence (AI) is the science and engineering of making intelligent machines and software capable of reasoning.',
    sections: [
      { heading: 'History and Evolution', content: 'Artificial intelligence evolved from symbolic reasoning and expert systems in the 1950s to statistical machine learning and modern deep artificial neural networks capable of complex perception.' },
      { heading: 'Subfields of AI', content: 'Major branches of artificial intelligence include Natural Language Processing (NLP), Computer Vision, Robotics, Automated Theorem Proving, and Reinforcement Learning.' }
    ]
  },
  {
    path: 'artificial-intelligence/artificial-neural-networks.txt',
    type: 'Article',
    date: '2024-04-10',
    category: 'artificial-intelligence',
    language: 'English',
    title: 'Artificial Neural Networks and Deep Learning',
    overview: 'Artificial neural networks are computing systems inspired by biological neural networks, forming the foundation of deep learning.',
    sections: [
      { heading: 'Network Architecture and Layers', content: 'A multi-layer perceptron (MLP) consists of an input layer, one or more hidden layers with non-linear activation functions (ReLU, GELU), and an output layer producing predictions.' },
      { heading: 'Backpropagation and Training', content: 'The backpropagation algorithm calculates partial derivatives of the loss function with respect to weights using the mathematical chain rule, enabling gradient-based parameter updates.' }
    ]
  },
  {
    path: 'artificial-intelligence/natural-language-processing.txt',
    type: 'Guide',
    date: '2024-04-12',
    category: 'artificial-intelligence',
    language: 'English',
    title: 'Natural Language Processing and Language Models',
    overview: 'Natural language processing (NLP) enables computers to understand, interpret, and manipulate human language text.',
    sections: [
      { heading: 'Text Representation and Embeddings', content: 'NLP pipelines process raw text through tokenization, stemming, and vectorization. Traditional representations like TF-IDF and Bag-of-Words have evolved into dense vector embeddings (Word2Vec, BERT embeddings).' },
      { heading: 'Transformer Architectures', content: 'The self-attention mechanism in Transformer architectures processes tokens in parallel, capturing long-range contextual dependencies for translation, summarization, and search relevance.' }
    ]
  },

  // 8. Cybersecurity
  {
    path: 'cybersecurity/network-security.txt',
    type: 'Wikipedia',
    date: '2024-04-15',
    category: 'cybersecurity',
    language: 'English',
    title: 'Network Security and Defense in Depth',
    overview: 'Network security consists of policies, processes, and technical defenses deployed to prevent unauthorized network access and attacks.',
    sections: [
      { heading: 'Perimeter and Internal Defenses', content: 'Network security employs next-generation firewalls (NGFW), stateful packet inspection, intrusion detection systems (IDS/IPS), and virtual private networks (VPNs) with IPsec/WireGuard.' },
      { heading: 'Threat Mitigation and Monitoring', content: 'Security Information and Event Management (SIEM) platforms aggregate network logs to detect port scans, distributed denial of service (DDoS) traffic, and lateral malware movement.' }
    ]
  },
  {
    path: 'cybersecurity/public-key-cryptography.txt',
    type: 'Article',
    date: '2024-04-18',
    category: 'cybersecurity',
    language: 'English',
    title: 'Public Key Cryptography and Digital Certificates',
    overview: 'Public-key cryptography uses mathematically linked public and private key pairs for secure encryption and identity authentication.',
    sections: [
      { heading: 'Asymmetric Algorithms: RSA and ECC', content: 'RSA relies on the computational difficulty of factoring large prime numbers, while Elliptic Curve Cryptography (ECC) provides equivalent cryptographic security with shorter keys.' },
      { heading: 'Digital Signatures and TLS Handshake', content: 'Digital signatures provide message integrity and non-repudiation. Transport Layer Security (TLS) uses asymmetric encryption during handshakes to securely negotiate symmetric session keys.' }
    ]
  },
  {
    path: 'cybersecurity/web-application-vulnerabilities.txt',
    type: 'Guide',
    date: '2024-04-20',
    category: 'cybersecurity',
    language: 'English',
    title: 'Web Application Vulnerabilities and OWASP Defenses',
    overview: 'Web application security focuses on identifying and mitigating security flaws in web servers, APIs, and client applications.',
    sections: [
      { heading: 'Injection and Cross-Site Scripting', content: 'SQL injection occurs when unsanitized user inputs manipulate database queries; parameterized queries eliminate SQL injection. Cross-Site Scripting (XSS) executes malicious scripts in user browsers.' },
      { heading: 'Authentication and Access Control', content: 'Robust web security requires multi-factor authentication (MFA), secure HTTP-only cookies, token-based authentication (JWT), and strict role-based access control (RBAC).' }
    ]
  },

  // 9. Web Development
  {
    path: 'web-development/rest-api-design.txt',
    type: 'Wikipedia',
    date: '2024-04-22',
    category: 'web-development',
    language: 'English',
    title: 'REST API Design Principles and Best Practices',
    overview: 'Representational State Transfer (REST) is a software architectural style for building scalable distributed web services.',
    sections: [
      { heading: 'REST Constraints', content: 'Key REST constraints include client-server separation, statelessness, cacheability, uniform interface (URI resource naming), and layered system architecture.' },
      { heading: 'HTTP Verbs and Status Codes', content: 'RESTful web APIs map operations to HTTP methods: GET (retrieve), POST (create), PUT/PATCH (update), and DELETE (remove), returning standard HTTP status codes (200 OK, 201 Created, 400 Bad Request, 404 Not Found).' }
    ]
  },
  {
    path: 'web-development/client-side-rendering.txt',
    type: 'Article',
    date: '2024-04-25',
    category: 'web-development',
    language: 'English',
    title: 'Client-Side Rendering and Modern UI Frameworks',
    overview: 'Client-side rendering generates dynamic HTML in the user browser using JavaScript frameworks and reactive component models.',
    sections: [
      { heading: 'Virtual DOM and Component Trees', content: 'Modern frontend frameworks like React use a Virtual DOM to compute minimal DOM reconciliation diffs, updating UI components efficiently without full page reloads.' },
      { heading: 'State Management and Routing', content: 'Single-page applications manage UI state reactively, synchronizing URL route changes using client-side router history APIs and asynchronous REST API fetching.' }
    ]
  },

  // 10. Programming Languages
  {
    path: 'programming-languages/compiler-design.txt',
    type: 'Wikipedia',
    date: '2024-04-28',
    category: 'programming-languages',
    language: 'English',
    title: 'Compiler Design: Lexing, Parsing and Code Generation',
    overview: 'A compiler translates source code written in a high-level programming language into low-level machine code or bytecode.',
    sections: [
      { heading: 'Frontend: Lexical and Syntactic Analysis', content: 'The compiler frontend performs lexical analysis (tokenizing characters into tokens) and parsing to construct an Abstract Syntax Tree (AST) validating grammar rules.' },
      { heading: 'Backend: Optimization and Machine Code', content: 'Intermediate Representation (IR) allows target-independent optimization (dead code elimination, loop unrolling) before register allocation and target assembly code generation.' }
    ]
  },
  {
    path: 'programming-languages/garbage-collection-mechanisms.txt',
    type: 'Guide',
    date: '2024-05-01',
    category: 'programming-languages',
    language: 'English',
    title: 'Garbage Collection and Automatic Memory Management',
    overview: 'Garbage collection automatically reclaims heap memory allocated by programs when objects are no longer reachable.',
    sections: [
      { heading: 'Tracing Garbage Collectors', content: 'Mark-and-sweep collectors traverse root object references to mark reachable memory, sweeping unmarked memory. Generational collectors segregate young and old objects based on survival age.' },
      { heading: 'Reference Counting and Stop-the-World', content: 'Reference counting increments object reference counters, deallocating immediately upon reaching zero but struggling with cyclic references unless accompanied by cycle detectors.' }
    ]
  },

  // 11. Mathematics
  {
    path: 'mathematics/linear-algebra-for-computing.txt',
    type: 'Wikipedia',
    date: '2024-05-05',
    category: 'mathematics',
    language: 'English',
    title: 'Linear Algebra for Computing and Data Science',
    overview: 'Linear algebra is the branch of mathematics concerning vector spaces, linear mappings, and matrix transformations.',
    sections: [
      { heading: 'Vectors, Matrices and Dot Products', content: 'Vector dot products compute inner products and geometric angles between multi-dimensional vectors, providing the mathematical basis for TF-IDF cosine similarity calculations in search engines.' },
      { heading: 'Matrix Factorization and Eigenvalues', content: 'Singular Value Decomposition (SVD) and Principal Component Analysis (PCA) factorize high-dimensional matrices to extract latent semantic structures and compress feature spaces.' }
    ]
  },

  // 12. Science & Quantum Computing
  {
    path: 'science/quantum-computing-fundamentals.txt',
    type: 'Wikipedia',
    date: '2024-05-08',
    category: 'science',
    language: 'English',
    title: 'Quantum Computing Fundamentals and Qubits',
    overview: 'Quantum computing leverages quantum mechanical phenomena such as superposition and entanglement to perform complex computations.',
    sections: [
      { heading: 'Superposition and Entanglement', content: 'A quantum bit (qubit) can exist in a superposition of basis states |0> and |1>. Entanglement creates correlated quantum states across multiple qubits with non-classical properties.' },
      { heading: 'Quantum Algorithms', content: 'Shor algorithm factors integers in polynomial time on quantum computers, while Grover search algorithm finds entries in an unstructured database with quadratic O(sqrt(N)) speedup.' }
    ]
  },

  // 13. Technology & Cloud
  {
    path: 'technology/cloud-computing-architecture.txt',
    type: 'Wikipedia',
    date: '2024-05-10',
    category: 'technology',
    language: 'English',
    title: 'Cloud Computing Architecture and Distributed Systems',
    overview: 'Cloud computing delivers on-demand computing services over the internet with elastic scaling and resource pooling.',
    sections: [
      { heading: 'Service and Deployment Models', content: 'Cloud architectures provide Infrastructure as a Service (IaaS), Platform as a Service (PaaS), and Software as a Service (SaaS) deployed across public, private, and hybrid clouds.' },
      { heading: 'Distributed Consensus and Microservices', content: 'Microservice architectures decompose applications into independently deployable services coordinating via asynchronous messaging and distributed consensus protocols (Raft, Paxos).' }
    ]
  },

  // 14. Difficult Edge Cases (Short docs, repeated terms, negative distractors)
  {
    path: 'technology/short-definition-algorithm.txt',
    type: 'Reference',
    date: '2024-05-12',
    category: 'algorithms',
    language: 'English',
    title: 'Algorithm Definition',
    overview: 'An algorithm is a finite sequence of rigorous mathematical instructions used to solve a specific class of problems or perform computation.',
    sections: [
      { heading: 'Summary', content: 'Algorithms operate on structured input data to produce correct output values within bounded computational time and memory space limits.' }
    ]
  },
  {
    path: 'databases/short-definition-database.txt',
    type: 'Reference',
    date: '2024-05-14',
    category: 'databases',
    language: 'English',
    title: 'Database Definition',
    overview: 'A database is an organized collection of structured data stored electronically in a computer storage system.',
    sections: [
      { heading: 'Overview', content: 'Databases are controlled by database management systems providing data querying, transaction concurrency, indexing, and storage durability.' }
    ]
  },
  {
    path: 'operating-systems/repeated-terms-memory.txt',
    type: 'Article',
    date: '2024-05-16',
    category: 'operating-systems',
    language: 'English',
    title: 'Memory Systems and Memory Allocation Hierarchy',
    overview: 'Memory memory memory systems manage memory allocation, memory caches, virtual memory, physical memory, and heap memory buffers.',
    sections: [
      { heading: 'Memory Hierarchy', content: 'Computer memory architecture includes register memory, L1 memory cache, L2 memory cache, L3 memory cache, main RAM memory, and secondary memory swap storage. The memory controller coordinates memory read and memory write operations across memory buses to maintain memory coherence.' }
    ]
  },
  {
    path: 'science/unrelated-distractor-ancient-history.txt',
    type: 'Wikipedia',
    date: '2024-05-18',
    category: 'science',
    language: 'English',
    title: 'Ancient Mediterranean Archaeological Excavations',
    overview: 'Archaeological excavations in the Mediterranean basin reveal pottery artifacts, stone architecture, and ancient agricultural irrigation techniques.',
    sections: [
      { heading: 'Bronze Age Trade Routes', content: 'Bronze Age civilizations established maritime trade routes across the Aegean Sea, exchanging copper ingots, ceramic pottery, olive oil, and bronze tools between coastal settlements.' }
    ]
  },
  {
    path: 'science/unrelated-distractor-oceanography.txt',
    type: 'Wikipedia',
    date: '2024-05-20',
    category: 'science',
    language: 'English',
    title: 'Oceanographic Currents and Marine Ecosystems',
    overview: 'Oceanography studies marine ecosystems, thermo-haline ocean currents, and deep-sea benthic biodiversity.',
    sections: [
      { heading: 'Thermo-Haline Circulation', content: 'Global thermo-haline ocean circulation is driven by density gradients created by surface heat and freshwater fluxes, regulating global climatic temperatures and transporting nutrients across marine biomes.' }
    ]
  }
];

// Write document files
docs.forEach(doc => {
  const fullPath = path.join(baseDir, doc.path);
  const dir = path.dirname(fullPath);
  fs.mkdirSync(dir, { recursive: true });

  let text = `type: ${doc.type}\n`;
  text += `date: ${doc.date}\n`;
  text += `category: ${doc.category}\n`;
  text += `language: ${doc.language}\n`;
  text += `title: ${doc.title}\n`;
  text += `overview: ${doc.overview}\n\n`;

  doc.sections.forEach(sec => {
    text += `## ${sec.heading}\n${sec.content}\n\n`;
  });

  fs.writeFileSync(fullPath, text.trim() + '\n', 'utf8');
});

// Write manifest index.txt
const manifestLines = [
  '# Evaluation Dataset Manifest for Mini Search Engine',
  '# Covers 13 domains, realistic overlap, difficult edge cases, distractors',
  '',
  ...docs.map(d => d.path)
];
fs.writeFileSync(path.join(baseDir, 'index.txt'), manifestLines.join('\n'), 'utf8');

console.log(`Successfully generated ${docs.length} evaluation documents and manifest in ${baseDir}!`);
