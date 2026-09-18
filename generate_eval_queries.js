const fs = require('fs');
const path = require('path');

const outDir = path.resolve(__dirname, 'Mini_Search_Engine/src/main/resources/evaluation');

const queries = [
  // --- Operating Systems (Q01 - Q06) ---
  {
    id: 'Q01',
    query: 'operating system',
    type: 'smart',
    description: 'Core OS concepts, kernel, and system software',
    expectedDocs: ['operating-systems/operating-system-architecture.txt', 'operating-systems/process-scheduling.txt', 'operating-systems/memory-management.txt']
  },
  {
    id: 'Q02',
    query: 'process scheduling',
    type: 'smart',
    description: 'CPU process scheduling and algorithms',
    expectedDocs: ['operating-systems/process-scheduling.txt', 'operating-systems/operating-system-architecture.txt']
  },
  {
    id: 'Q03',
    query: 'memory management',
    type: 'smart',
    description: 'Virtual memory, paging, and page replacement',
    expectedDocs: ['operating-systems/memory-management.txt', 'operating-systems/repeated-terms-memory.txt', 'operating-systems/operating-system-architecture.txt']
  },
  {
    id: 'Q04',
    query: '"virtual memory"',
    type: 'exact',
    description: 'Exact phrase search for virtual memory',
    expectedDocs: ['operating-systems/memory-management.txt', 'operating-systems/operating-system-architecture.txt', 'operating-systems/repeated-terms-memory.txt']
  },
  {
    id: 'Q05',
    query: 'deadlock handling synchronization',
    type: 'smart',
    description: 'Concurrency, semaphores, mutexes, and deadlocks',
    expectedDocs: ['operating-systems/concurrency-and-deadlocks.txt']
  },
  {
    id: 'Q06',
    query: 'filesystem inodes journaling',
    type: 'smart',
    description: 'Persistent file storage, metadata inodes, crash consistency',
    expectedDocs: ['operating-systems/filesystem-architecture.txt']
  },

  // --- Algorithms (Q07 - Q12) ---
  {
    id: 'Q07',
    query: 'graph algorithms',
    type: 'smart',
    description: 'Graph search, BFS, DFS, and Dijkstra',
    expectedDocs: ['algorithms/graph-algorithms.txt']
  },
  {
    id: 'Q08',
    query: 'dynamic programming',
    type: 'smart',
    description: 'Optimal substructure, memoization, and tabulation',
    expectedDocs: ['algorithms/dynamic-programming.txt']
  },
  {
    id: 'Q09',
    query: 'quicksort mergesort',
    type: 'smart',
    description: 'Divide and conquer sorting algorithms',
    expectedDocs: ['algorithms/sorting-algorithms.txt']
  },
  {
    id: 'Q10',
    query: 'string matching algorithms',
    type: 'smart',
    description: 'KMP, Aho-Corasick, Rabin-Karp, Z-algorithm',
    expectedDocs: ['algorithms/string-matching-algorithms.txt']
  },
  {
    id: 'Q11',
    query: '"shortest path"',
    type: 'exact',
    description: 'Exact phrase for shortest path traversal',
    expectedDocs: ['algorithms/graph-algorithms.txt', 'algorithms/dynamic-programming.txt', 'computer-networks/network-routing-algorithms.txt']
  },
  {
    id: 'Q12',
    query: 'breadth-first search traversal',
    type: 'smart',
    description: 'BFS exploration using queue',
    expectedDocs: ['algorithms/graph-algorithms.txt']
  },

  // --- Data Structures (Q13 - Q17) ---
  {
    id: 'Q13',
    query: 'b-tree database indexing',
    type: 'smart',
    description: 'B-tree and B+ tree storage index structures',
    expectedDocs: ['data-structures/b-trees-and-b-plus-trees.txt', 'databases/database-indexing.txt']
  },
  {
    id: 'Q14',
    query: 'balanced binary search tree',
    type: 'smart',
    description: 'Red-Black and AVL balanced trees',
    expectedDocs: ['data-structures/red-black-and-avl-trees.txt']
  },
  {
    id: 'Q15',
    query: 'hash table collision resolution',
    type: 'smart',
    description: 'Hashing, chaining, and open addressing',
    expectedDocs: ['data-structures/hash-tables-and-collision-resolution.txt']
  },
  {
    id: 'Q16',
    query: 'heap priority queue',
    type: 'smart',
    description: 'Binary heap, extract-min, and priority queues',
    expectedDocs: ['data-structures/heaps-and-priority-queues.txt']
  },
  {
    id: 'Q17',
    query: 'trie prefix tree autocomplete',
    type: 'smart',
    description: 'Prefix matching, radix compaction, and autocomplete',
    expectedDocs: ['data-structures/trie-and-radix-trees.txt']
  },

  // --- Databases (Q18 - Q22) ---
  {
    id: 'Q18',
    query: 'database indexing',
    type: 'smart',
    description: 'Database index structures and query performance',
    expectedDocs: ['databases/database-indexing.txt', 'data-structures/b-trees-and-b-plus-trees.txt']
  },
  {
    id: 'Q19',
    query: 'relational database sql',
    type: 'smart',
    description: 'RDBMS, schema constraints, joins, and normal forms',
    expectedDocs: ['databases/relational-database-management-systems.txt', 'databases/short-definition-database.txt']
  },
  {
    id: 'Q20',
    query: 'nosql document stores',
    type: 'smart',
    description: 'Non-relational data models and CAP theorem',
    expectedDocs: ['databases/nosql-document-stores.txt']
  },
  {
    id: 'Q21',
    query: 'acid transactions isolation',
    type: 'smart',
    description: 'ACID properties, isolation levels, write-ahead logging',
    expectedDocs: ['databases/acid-transactions-and-isolation.txt']
  },
  {
    id: 'Q22',
    query: '"cost-based"',
    type: 'exact',
    description: 'Cost-based query optimization in databases',
    expectedDocs: ['databases/database-indexing.txt']
  },

  // --- Computer Networks (Q23 - Q27) ---
  {
    id: 'Q23',
    query: 'tcp ip protocol',
    type: 'smart',
    description: 'TCP/IP layered architecture and reliable transport',
    expectedDocs: ['computer-networks/tcp-ip-protocol-suite.txt', 'computer-networks/network-routing-algorithms.txt']
  },
  {
    id: 'Q24',
    query: 'network routing algorithms',
    type: 'smart',
    description: 'OSPF, RIP, BGP, and routing path algorithms',
    expectedDocs: ['computer-networks/network-routing-algorithms.txt', 'algorithms/graph-algorithms.txt']
  },
  {
    id: 'Q25',
    query: 'domain name system',
    type: 'smart',
    description: 'DNS resolution, root servers, and DNS records',
    expectedDocs: ['computer-networks/domain-name-system.txt']
  },
  {
    id: 'Q26',
    query: '"three-way handshake"',
    type: 'exact',
    description: 'TCP connection establishment handshake',
    expectedDocs: ['computer-networks/tcp-ip-protocol-suite.txt']
  },
  {
    id: 'Q27',
    query: 'congestion control sliding window',
    type: 'smart',
    description: 'TCP flow control and congestion avoidance',
    expectedDocs: ['computer-networks/tcp-ip-protocol-suite.txt']
  },

  // --- Machine Learning (Q28 - Q32) ---
  {
    id: 'Q28',
    query: 'machine learning algorithms',
    type: 'smart',
    description: 'Supervised, unsupervised, and reinforcement learning paradigms',
    expectedDocs: ['machine-learning/machine-learning-algorithms.txt', 'machine-learning/supervised-learning.txt', 'machine-learning/gradient-descent-optimization.txt']
  },
  {
    id: 'Q29',
    query: 'gradient descent optimization',
    type: 'smart',
    description: 'Loss functions, SGD, Adam optimizer',
    expectedDocs: ['machine-learning/gradient-descent-optimization.txt', 'machine-learning/machine-learning-algorithms.txt']
  },
  {
    id: 'Q30',
    query: 'supervised learning classification regression',
    type: 'smart',
    description: 'Logistic regression, SVM, Ridge and Lasso regression',
    expectedDocs: ['machine-learning/supervised-learning.txt', 'machine-learning/machine-learning-algorithms.txt']
  },
  {
    id: 'Q31',
    query: '"overfitting"',
    type: 'exact',
    description: 'Model generalization and regularization against overfitting',
    expectedDocs: ['machine-learning/machine-learning-algorithms.txt']
  },
  {
    id: 'Q32',
    query: 'support vector machines kernel',
    type: 'smart',
    description: 'SVM classification with kernel trick',
    expectedDocs: ['machine-learning/supervised-learning.txt']
  },

  // --- Artificial Intelligence (Q33 - Q37) ---
  {
    id: 'Q33',
    query: 'artificial intelligence',
    type: 'smart',
    description: 'AI history, intelligent agents, and symbolic reasoning',
    expectedDocs: ['artificial-intelligence/artificial-intelligence.txt', 'artificial-intelligence/artificial-neural-networks.txt', 'artificial-intelligence/natural-language-processing.txt']
  },
  {
    id: 'Q34',
    query: 'artificial neural networks',
    type: 'smart',
    description: 'Perceptrons, backpropagation, and deep learning layers',
    expectedDocs: ['artificial-intelligence/artificial-neural-networks.txt', 'artificial-intelligence/artificial-intelligence.txt']
  },
  {
    id: 'Q35',
    query: 'natural language processing',
    type: 'smart',
    description: 'NLP tokenization, TF-IDF, embeddings, and Transformers',
    expectedDocs: ['artificial-intelligence/natural-language-processing.txt', 'artificial-intelligence/artificial-intelligence.txt']
  },
  {
    id: 'Q36',
    query: '"backpropagation algorithm"',
    type: 'exact',
    description: 'Backpropagation gradient calculation via chain rule',
    expectedDocs: ['artificial-intelligence/artificial-neural-networks.txt']
  },
  {
    id: 'Q37',
    query: 'transformer attention mechanism',
    type: 'smart',
    description: 'Self-attention mechanism and Transformer language models',
    expectedDocs: ['artificial-intelligence/natural-language-processing.txt']
  },

  // --- Cybersecurity (Q38 - Q41) ---
  {
    id: 'Q38',
    query: 'network security',
    type: 'smart',
    description: 'Firewalls, IDS/IPS, VPN, and perimeter security',
    expectedDocs: ['cybersecurity/network-security.txt', 'cybersecurity/public-key-cryptography.txt']
  },
  {
    id: 'Q39',
    query: 'public key cryptography',
    type: 'smart',
    description: 'Asymmetric encryption, RSA, ECC, and digital signatures',
    expectedDocs: ['cybersecurity/public-key-cryptography.txt', 'cybersecurity/network-security.txt']
  },
  {
    id: 'Q40',
    query: 'web application vulnerabilities sql injection',
    type: 'smart',
    description: 'OWASP vulnerabilities, SQL injection, and XSS defenses',
    expectedDocs: ['cybersecurity/web-application-vulnerabilities.txt']
  },
  {
    id: 'Q41',
    query: '"digital signatures"',
    type: 'exact',
    description: 'Exact phrase for cryptographic digital signatures',
    expectedDocs: ['cybersecurity/public-key-cryptography.txt']
  },

  // --- Web Development & Programming Languages (Q42 - Q46) ---
  {
    id: 'Q42',
    query: 'rest api design',
    type: 'smart',
    description: 'REST architecture, HTTP methods, and status codes',
    expectedDocs: ['web-development/rest-api-design.txt']
  },
  {
    id: 'Q43',
    query: 'client side rendering virtual dom',
    type: 'smart',
    description: 'SPA rendering, Virtual DOM reconciliation, and state',
    expectedDocs: ['web-development/client-side-rendering.txt']
  },
  {
    id: 'Q44',
    query: 'compiler design lexical parsing',
    type: 'smart',
    description: 'Lexing, AST parsing, and code generation',
    expectedDocs: ['programming-languages/compiler-design.txt']
  },
  {
    id: 'Q45',
    query: 'garbage collection automatic memory',
    type: 'smart',
    description: 'Mark-and-sweep, generational GC, and reference counting',
    expectedDocs: ['programming-languages/garbage-collection-mechanisms.txt']
  },
  {
    id: 'Q46',
    query: '"abstract syntax tree"',
    type: 'exact',
    description: 'Exact phrase for compiler AST representations',
    expectedDocs: ['programming-languages/compiler-design.txt', 'databases/relational-database-management-systems.txt']
  },

  // --- Mathematics, Science & Cloud (Q47 - Q50) ---
  {
    id: 'Q47',
    query: 'linear algebra vector dot products',
    type: 'smart',
    description: 'Linear algebra, matrix factorization, and cosine similarity',
    expectedDocs: ['mathematics/linear-algebra-for-computing.txt']
  },
  {
    id: 'Q48',
    query: 'quantum computing qubits superposition',
    type: 'smart',
    description: 'Quantum superposition, entanglement, Shor and Grover',
    expectedDocs: ['science/quantum-computing-fundamentals.txt']
  },
  {
    id: 'Q49',
    query: 'cloud computing microservices architecture',
    type: 'smart',
    description: 'Cloud deployment, IaaS/PaaS, and distributed consensus',
    expectedDocs: ['technology/cloud-computing-architecture.txt']
  },
  {
    id: 'Q50',
    query: 'computer architecture',
    type: 'smart',
    description: 'Computer systems architecture and memory hierarchy',
    expectedDocs: ['operating-systems/operating-system-architecture.txt', 'operating-systems/repeated-terms-memory.txt']
  },

  // --- Multi-Pattern & Typo / Fuzzy Search Queries (Q51 - Q55) ---
  {
    id: 'Q51',
    query: 'kmp, aho-corasick, rabin-karp',
    type: 'multi-pattern',
    description: 'Multi-pattern string algorithm query',
    expectedDocs: ['algorithms/string-matching-algorithms.txt']
  },
  {
    id: 'Q52',
    query: 'machne learnng',
    type: 'fuzzy',
    description: 'Fuzzy typo query for machine learning',
    expectedDocs: ['machine-learning/machine-learning-algorithms.txt', 'machine-learning/supervised-learning.txt']
  },
  {
    id: 'Q53',
    query: 'algoritm',
    type: 'fuzzy',
    description: 'Fuzzy typo query for algorithm',
    expectedDocs: ['technology/short-definition-algorithm.txt', 'algorithms/graph-algorithms.txt', 'algorithms/dynamic-programming.txt', 'algorithms/sorting-algorithms.txt']
  },
  {
    id: 'Q54',
    query: 'relatinal datbase',
    type: 'fuzzy',
    description: 'Fuzzy typo query for relational database',
    expectedDocs: ['databases/relational-database-management-systems.txt', 'databases/short-definition-database.txt']
  },
  {
    id: 'Q55',
    query: 'proccess schedulng',
    type: 'fuzzy',
    description: 'Fuzzy typo query for process scheduling',
    expectedDocs: ['operating-systems/process-scheduling.txt', 'operating-systems/operating-system-architecture.txt']
  }
];

// Write queries.json
fs.writeFileSync(path.join(outDir, 'queries.json'), JSON.stringify(queries, null, 2), 'utf8');

// Write qrels.txt (TREC qrels format: query_id 0 doc_path relevance_grade)
const qrelLines = [];
queries.forEach(q => {
  q.expectedDocs.forEach((docPath, rankIdx) => {
    // 2 for top relevance, 1 for secondary relevance
    const grade = rankIdx === 0 ? 2 : 1;
    qrelLines.push(`${q.id}\t0\t${docPath}\t${grade}`);
  });
});
fs.writeFileSync(path.join(outDir, 'qrels.txt'), qrelLines.join('\n'), 'utf8');

console.log(`Successfully generated ${queries.length} evaluation queries and qrels in ${outDir}!`);
