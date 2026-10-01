import os
import re
from datasets import load_dataset

SUBSETS = [
    ("20231101.ab", "abkhazian"),
    ("20231101.ace", "acehnese"),
    ("20231101.ady", "adyghe")
]

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
CORPUS_DIR = os.path.join(PROJECT_ROOT, "Backend", "src", "main", "resources", "corpus")

def sanitize_slug(title, doc_id):
    # Keep alphanumeric, hyphen, underscore
    slug = re.sub(r'[^\w\s-]', '', title).strip().lower()
    slug = re.sub(r'[\s_]+', '-', slug)
    if not slug:
        slug = f"wiki-{doc_id}"
    return slug

manifest_entries = []

for subset_code, category in SUBSETS:
    print(f"Loading dataset: wikimedia/wikipedia ({subset_code})...")
    try:
        ds = load_dataset("wikimedia/wikipedia", subset_code, split="train", streaming=True)
    except Exception as e:
        print(f"Error loading {subset_code}: {e}")
        continue
    
    category_dir = os.path.join(CORPUS_DIR, category)
    os.makedirs(category_dir, exist_ok=True)
    
    count = 0
    for item in ds:
        title = item.get("title", "").strip()
        text = item.get("text", "").strip()
        doc_id = item.get("id", f"{count+1}")
        
        if not title or len(text) < 50:
            continue
            
        slug = sanitize_slug(title, doc_id)
        filename = f"{slug}.txt"
        file_path = os.path.join(category_dir, filename)
        
        # Format overview (first 200 chars or first paragraph)
        paragraphs = [p.strip() for p in text.split("\n") if p.strip()]
        overview = paragraphs[0] if paragraphs else text[:200]
        if len(overview) > 300:
            overview = overview[:297] + "..."
            
        rel_path = f"{category}/{filename}"
        manifest_entries.append(rel_path)
        
        # Write file with front-matter
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(f"type: Wikipedia\n")
            f.write(f"date: 2023-11-01\n")
            f.write(f"title: {title}\n")
            f.write(f"overview: {overview}\n\n")
            
            # Format sections from paragraphs if available
            if len(paragraphs) > 1:
                f.write(f"## Overview\n{paragraphs[0]}\n\n")
                for idx, p in enumerate(paragraphs[1:], 1):
                    f.write(f"## Section {idx}\n{p}\n\n")
            else:
                f.write(f"## Content\n{text}\n")
                
        count += 1
        if count >= MAX_ARTICLES_PER_SUBSET:
            break

print(f"Successfully processed {len(manifest_entries)} Wikipedia articles.")

# Write updated index.txt manifest
manifest_path = os.path.join(CORPUS_DIR, "index.txt")
with open(manifest_path, "w", encoding="utf-8") as f:
    f.write("# Corpus manifest file for Mini Search Engine (Wikipedia datasets)\n")
    for entry in manifest_entries:
        f.write(f"{entry}\n")

print(f"Updated corpus manifest at {manifest_path}")
