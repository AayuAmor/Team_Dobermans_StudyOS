
import re
import logging
from collections import Counter
from dataclasses import dataclass
from typing import Any, Dict, List, Optional

logger = logging.getLogger(__name__)

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

@dataclass
class _StyleConfig:
    summary_sentences: int
    key_points: int
    max_sections: int
    bullets_per_section: int
    max_terms: int
    max_questions: int

_STYLE_CONFIGS: Dict[str, _StyleConfig] = {
    "short":    _StyleConfig(summary_sentences=2, key_points=4,  max_sections=2, bullets_per_section=3, max_terms=6,  max_questions=5),
    "detailed": _StyleConfig(summary_sentences=4, key_points=8,  max_sections=4, bullets_per_section=5, max_terms=10, max_questions=10),
    "exam":     _StyleConfig(summary_sentences=2, key_points=5,  max_sections=2, bullets_per_section=4, max_terms=12, max_questions=15),
    "bullets":  _StyleConfig(summary_sentences=3, key_points=15, max_sections=2, bullets_per_section=5, max_terms=8,  max_questions=8),
}
_DEFAULT_CONFIG = _STYLE_CONFIGS["detailed"]

                                                                                

def generate_notes(transcript: str, title: str = "", summary_style: str = "detailed") -> Dict[str, Any]:
    transcript = transcript.strip()
    if not transcript:
        return _empty_notes()

    cfg = _STYLE_CONFIGS.get(summary_style.lower(), _DEFAULT_CONFIG)
    logger.info(f"Generating study notes (style={summary_style!r})...")

    sentences = _split_sentences(transcript)
    word_freq = _word_frequency(_tokenize(transcript))

    result = {
        "summary":            _build_summary(sentences, n=cfg.summary_sentences),
        "key_points":         _extract_key_points(sentences, word_freq, n=cfg.key_points),
        "important_terms":    _extract_important_terms(transcript, sentences, word_freq, max_terms=cfg.max_terms),
        "study_notes":        _build_study_sections(sentences, word_freq, max_sections=cfg.max_sections, bullets_per_section=cfg.bullets_per_section),
        "possible_questions": _build_questions(sentences, word_freq, title, max_q=cfg.max_questions),
    }

    logger.info("Notes generation complete.")
    return result

                                                                                 

def _split_sentences(text: str) -> List[str]:
    text = re.sub(r"\s+", " ", text).strip()
    raw = re.split(r"(?<=[.!?])\s+", text)
    return [s.strip() for s in raw if len(s.strip()) > 25]

def _tokenize(text: str) -> List[str]:
    return [w for w in re.findall(r"\b[a-zA-Z]{3,}\b", text.lower())]

def _word_frequency(words: List[str]) -> Counter:
    return Counter(w for w in words if w not in _STOP_WORDS)

def _score_sentence(sentence: str, freq: Counter) -> float:
    words = [w for w in _tokenize(sentence) if w not in _STOP_WORDS]
    if not words:
        return 0.0
    return sum(freq.get(w, 0) for w in words) / len(words)

def _clip(text: str, limit: int) -> str:
    return text if len(text) <= limit else text[: limit - 3] + "..."

                                                                                

def _build_summary(sentences: List[str], n: int = 4) -> str:
    if not sentences:
        return "No content available."
    intro = " ".join(sentences[:n])
    return _clip(intro, 900)

                                                                                

def _extract_key_points(sentences: List[str], freq: Counter, n: int = 8) -> List[str]:
    if not sentences:
        return []
    scored = sorted(sentences, key=lambda s: _score_sentence(s, freq), reverse=True)
    top_set = set(scored[:n])
    ordered = [s for s in sentences if s in top_set]
    return [_clip(s, 260) for s in ordered]

                                                                                

def _extract_important_terms(
    text: str, sentences: List[str], freq: Counter, max_terms: int = 10
) -> List[Dict[str, str]]:
    terms: Dict[str, str] = {}

    cap_phrases = re.findall(r"\b([A-Z][a-z]+(?:\s+[A-Z][a-z]+){1,3})\b", text)
    for phrase, count in Counter(cap_phrases).most_common(max_terms):
        if count >= 2 and phrase.lower() not in _STOP_WORDS and phrase not in terms:
            terms[phrase] = _find_first_context(phrase, sentences)
        if len(terms) >= max_terms // 2:
            break

    for word, count in freq.most_common(40):
        if count >= 3 and len(word) > 4:
            display = word.capitalize()
            if display not in terms:
                ctx = _find_first_context(word, sentences)
                if ctx:
                    terms[display] = ctx
        if len(terms) >= max_terms:
            break

    return [{"term": t, "meaning": m} for t, m in list(terms.items())[:max_terms]]

def _find_first_context(term: str, sentences: List[str]) -> str:
    for s in sentences:
        if term.lower() in s.lower():
            return _clip(s, 180)
    return "A key concept discussed in this content."

                                                                                

def _build_study_sections(
    sentences: List[str], freq: Counter, max_sections: int = 4, bullets_per_section: int = 5
) -> List[Dict[str, Any]]:
    if not sentences:
        return []

    total = len(sentences)
    n_sections = max(2, min(max_sections, total // 6))
    chunk_size = max(2, total // n_sections)

    sections = []
    for i in range(n_sections):
        start = i * chunk_size
        end = (start + chunk_size) if i < n_sections - 1 else total
        chunk = sentences[start:end]
        if not chunk:
            continue

        heading = _make_heading(chunk, freq, i + 1)
        top = sorted(chunk, key=lambda s: _score_sentence(s, freq), reverse=True)[:bullets_per_section]
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

                                                                                

def _build_questions(
    sentences: List[str], freq: Counter, title: str, max_q: int = 10
) -> List[str]:
    questions: List[str] = []

    if title and title.lower() not in ("unknown video", "uploaded file", ""):
        questions.append(f"What is the main topic covered in '{title}'?")

    key = _extract_key_points(sentences, freq, n=min(8, max_q))
    for s in key:
        q = _sentence_to_question(s)
        if q:
            questions.append(q)

    terms = _extract_important_terms("", sentences, freq, max_terms=min(6, max_q))
    for item in terms[:6]:
        questions.append(f"What is '{item['term']}' and why is it important here?")

    questions += [
        "What are the three most important takeaways from this content?",
        "How would you explain this topic to someone hearing it for the first time?",
        "What real-world applications do the concepts in this content have?",
        "What prior knowledge is assumed by this content?",
        "How do the key concepts in this content relate to each other?",
        "What questions does this content leave unanswered?",
        "Summarize this content in a single sentence.",
        "What would be a good follow-up resource for this topic?",
    ]

    seen: set = set()
    unique: List[str] = []
    for q in questions:
        if q not in seen:
            seen.add(q)
            unique.append(q)

    return unique[:max_q]

def _sentence_to_question(sentence: str) -> Optional[str]:
    s = sentence.strip().rstrip(".")
    if not s or len(s.split()) < 5:
        return None

    match = re.match(r"^([A-Z][^,]+?)\s+is\s+", s)
    if match:
        subject = match.group(1).strip()
        if len(subject.split()) <= 6:
            return f"What is {subject}?"

    match = re.match(r"^([A-Z][^,]+?)\s+are\s+", s)
    if match:
        subject = match.group(1).strip()
        if len(subject.split()) <= 6:
            return f"What are {subject}?"

    words = s.split()
    snippet = " ".join(words[:7])
    return f"Explain the concept of: '{snippet}...'?"

                                                                                

def _empty_notes() -> Dict[str, Any]:
    return {
        "summary": "No transcript available to generate notes.",
        "key_points": [],
        "important_terms": [],
        "study_notes": [],
        "possible_questions": [],
    }
