"""
Pure-Python study notes generator.

Designed for easy replacement: swap generate_notes() with an LLM/RAG
implementation and the rest of the codebase stays unchanged.
"""

import re
import logging
from collections import Counter
from typing import Any, Dict, List, Optional

logger = logging.getLogger(__name__)

# Broad English stop-word list for keyword extraction
_STOP_WORDS = frozenset({
    "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
    "of", "with", "by", "from", "is", "it", "its", "as", "be", "was",
    "are", "were", "been", "has", "have", "had", "do", "does", "did",
    "not", "that", "this", "these", "those", "i", "we", "you", "he",
    "she", "they", "me", "him", "her", "us", "them", "my", "our", "your",
    "his", "their", "what", "which", "who", "will", "would", "could",
    "should", "can", "may", "might", "so", "if", "then", "than", "when",
    "there", "here", "also", "just", "about", "more", "other", "some",
    "such", "like", "well", "into", "out", "up", "down", "after",
    "before", "over", "under", "between", "through", "because", "while",
    "get", "got", "let", "set", "put", "say", "said", "know", "think",
    "go", "going", "make", "made", "see", "use", "used", "want", "way",
    "one", "two", "three", "first", "second", "third", "now", "even",
    "very", "much", "many", "most", "any", "all", "new", "old",
})


# ─── Public API ───────────────────────────────────────────────────────────────

def generate_notes(transcript: str, title: str = "") -> Dict[str, Any]:
    """
    Convert a raw transcript into structured study notes.

    Returns a dict matching the NotesOutput schema:
        summary, key_points, important_terms, study_notes, possible_questions
    """
    transcript = transcript.strip()
    if not transcript:
        return _empty_notes()

    logger.info("Generating study notes from transcript...")

    sentences = _split_sentences(transcript)
    word_freq = _word_frequency(_tokenize(transcript))

    result = {
        "summary": _build_summary(sentences),
        "key_points": _extract_key_points(sentences, word_freq),
        "important_terms": _extract_important_terms(transcript, sentences, word_freq),
        "study_notes": _build_study_sections(sentences, word_freq),
        "possible_questions": _build_questions(sentences, word_freq, title),
    }

    logger.info("Notes generation complete.")
    return result


# ─── Sentence utilities ────────────────────────────────────────────────────────

def _split_sentences(text: str) -> List[str]:
    """Split text on sentence-ending punctuation."""
    text = re.sub(r"\s+", " ", text).strip()
    raw = re.split(r"(?<=[.!?])\s+", text)
    return [s.strip() for s in raw if len(s.strip()) > 25]


def _tokenize(text: str) -> List[str]:
    return [w for w in re.findall(r"\b[a-zA-Z]{3,}\b", text.lower())]


def _word_frequency(words: List[str]) -> Counter:
    return Counter(w for w in words if w not in _STOP_WORDS)


def _score_sentence(sentence: str, freq: Counter) -> float:
    """Rank a sentence by the average frequency of its significant words."""
    words = [w for w in _tokenize(sentence) if w not in _STOP_WORDS]
    if not words:
        return 0.0
    return sum(freq.get(w, 0) for w in words) / len(words)


def _clip(text: str, limit: int) -> str:
    return text if len(text) <= limit else text[: limit - 3] + "..."


# ─── Summary ──────────────────────────────────────────────────────────────────

def _build_summary(sentences: List[str]) -> str:
    """
    Use the first four meaningful sentences as an intro summary.
    Transcripts typically start with the topic intro, making position a good heuristic.
    """
    if not sentences:
        return "No content available."
    intro = " ".join(sentences[:4])
    return _clip(intro, 900)


# ─── Key points ───────────────────────────────────────────────────────────────

def _extract_key_points(sentences: List[str], freq: Counter, n: int = 8) -> List[str]:
    """Return the top-N highest-scoring sentences in reading order."""
    if not sentences:
        return []

    scored = sorted(sentences, key=lambda s: _score_sentence(s, freq), reverse=True)
    top_set = set(scored[:n])
    # Restore document order
    ordered = [s for s in sentences if s in top_set]
    return [_clip(s, 260) for s in ordered]


# ─── Important terms ──────────────────────────────────────────────────────────

def _extract_important_terms(
    text: str, sentences: List[str], freq: Counter
) -> List[Dict[str, str]]:
    """
    Identify key terms using two strategies:
    1. Capitalized multi-word phrases (proper nouns, named concepts)
    2. High-frequency single content words
    Attaches the first sentence that uses the term as its 'meaning'.
    """
    terms: Dict[str, str] = {}

    # Strategy 1: repeated capitalized phrases (2–4 words)
    cap_phrases = re.findall(r"\b([A-Z][a-z]+(?:\s+[A-Z][a-z]+){1,3})\b", text)
    for phrase, count in Counter(cap_phrases).most_common(10):
        if count >= 2 and phrase.lower() not in _STOP_WORDS and phrase not in terms:
            terms[phrase] = _find_first_context(phrase, sentences)
        if len(terms) >= 6:
            break

    # Strategy 2: high-frequency content words
    for word, count in freq.most_common(30):
        if count >= 3 and len(word) > 4:
            display = word.capitalize()
            if display not in terms:
                ctx = _find_first_context(word, sentences)
                if ctx:
                    terms[display] = ctx
        if len(terms) >= 12:
            break

    return [{"term": t, "meaning": m} for t, m in list(terms.items())[:10]]


def _find_first_context(term: str, sentences: List[str]) -> str:
    for s in sentences:
        if term.lower() in s.lower():
            return _clip(s, 180)
    return "A key concept discussed in this content."


# ─── Study sections ───────────────────────────────────────────────────────────

def _build_study_sections(
    sentences: List[str], freq: Counter
) -> List[Dict[str, Any]]:
    """
    Chunk the transcript into 3–4 sections and pick the most informative
    sentences from each chunk as bullet points.
    """
    if not sentences:
        return []

    total = len(sentences)
    n_sections = max(2, min(4, total // 6))
    chunk_size = max(2, total // n_sections)

    sections = []
    for i in range(n_sections):
        start = i * chunk_size
        end = (start + chunk_size) if i < n_sections - 1 else total
        chunk = sentences[start:end]
        if not chunk:
            continue

        heading = _make_heading(chunk, freq, i + 1)
        # Top 3–5 bullets from this chunk ranked by relevance
        top = sorted(chunk, key=lambda s: _score_sentence(s, freq), reverse=True)[:5]
        bullets = [_clip(s, 210) for s in top]

        sections.append({"heading": heading, "points": bullets})

    return sections


def _make_heading(chunk: List[str], freq: Counter, num: int) -> str:
    chunk_words = _tokenize(" ".join(chunk))
    chunk_freq = Counter(w for w in chunk_words if w not in _STOP_WORDS)
    top_words = [w.capitalize() for w, _ in chunk_freq.most_common(3)]
    if top_words:
        return f"Section {num}: {' & '.join(top_words[:2])}"
    return f"Section {num}: Core Concepts"


# ─── Possible questions ───────────────────────────────────────────────────────

def _build_questions(
    sentences: List[str], freq: Counter, title: str
) -> List[str]:
    questions: List[str] = []

    if title and title.lower() not in ("unknown video", "uploaded file", ""):
        questions.append(f"What is the main topic covered in '{title}'?")

    # Convert top key-point sentences into questions
    key = _extract_key_points(sentences, freq, n=6)
    for s in key:
        q = _sentence_to_question(s)
        if q:
            questions.append(q)

    # Term-based questions from important terms
    terms = _extract_important_terms("", sentences, freq)
    for item in terms[:5]:
        questions.append(f"What is '{item['term']}' and why is it important here?")

    # Universal study questions
    questions += [
        "What are the three most important takeaways from this content?",
        "How would you explain this topic to someone hearing it for the first time?",
        "What real-world applications do the concepts in this content have?",
        "What prior knowledge is assumed by this content?",
    ]

    # Deduplicate while preserving order
    seen: set = set()
    unique: List[str] = []
    for q in questions:
        if q not in seen:
            seen.add(q)
            unique.append(q)

    return unique[:10]


def _sentence_to_question(sentence: str) -> Optional[str]:
    """Heuristically convert a declarative sentence into a study question."""
    s = sentence.strip().rstrip(".")
    if not s or len(s.split()) < 5:
        return None

    lower = s.lower()

    # "X is Y" → "What is X?"
    match = re.match(r"^([A-Z][^,]+?)\s+is\s+", s)
    if match:
        subject = match.group(1).strip()
        if len(subject.split()) <= 6:
            return f"What is {subject}?"

    # "X are Y" → "What are X?"
    match = re.match(r"^([A-Z][^,]+?)\s+are\s+", s)
    if match:
        subject = match.group(1).strip()
        if len(subject.split()) <= 6:
            return f"What are {subject}?"

    # Generic fallback: "Explain: <first 7 words>..."
    words = s.split()
    snippet = " ".join(words[:7])
    return f"Explain the concept of: '{snippet}...'?"


# ─── Helpers ──────────────────────────────────────────────────────────────────

def _empty_notes() -> Dict[str, Any]:
    return {
        "summary": "No transcript available to generate notes.",
        "key_points": [],
        "important_terms": [],
        "study_notes": [],
        "possible_questions": [],
    }
