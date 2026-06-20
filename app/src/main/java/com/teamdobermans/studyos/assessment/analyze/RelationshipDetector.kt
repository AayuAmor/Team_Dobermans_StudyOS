package com.teamdobermans.studyos.assessment.analyze

import com.teamdobermans.studyos.assessment.model.ExtractedRelationship

class RelationshipDetector {

    private data class RelPattern(val regex: Regex, val predicate: String, val objectGroup: Int)

    private val patterns = listOf(
        RelPattern(
            Regex("""(.{3,40}?)\s+is\s+used\s+(?:to|for)\s+(.{5,60})""", RegexOption.IGNORE_CASE),
            "is used for", 2
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(enables?|allows?|helps?)\s+(.{5,60})""", RegexOption.IGNORE_CASE),
            "enables", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(contains?|includes?|consists?\s+of)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "contains", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(requires?|needs?|depends?\s+on)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "requires", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(produces?|generates?|creates?|returns?|outputs?)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "produces", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(extends?|implements?|inherits?\s+from)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "extends", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(provides?|offers?|gives?)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "provides", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(stores?|holds?|maintains?|keeps?)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "stores", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(converts?|transforms?|changes?)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "converts", 3
        ),
        RelPattern(
            Regex("""(.{3,40}?)\s+(controls?|manages?|handles?)\s+(.{3,60})""", RegexOption.IGNORE_CASE),
            "controls", 3
        )
    )

    fun detect(sentences: List<String>): List<ExtractedRelationship> {
        val results = mutableListOf<ExtractedRelationship>()
        val seen = mutableSetOf<String>()

        for (sentence in sentences) {
            for (pattern in patterns) {
                val match = pattern.regex.find(sentence) ?: continue
                val subject    = match.groupValues[1].trim().trimEnd(',')
                val objectTerm = match.groupValues[pattern.objectGroup].trim().trimEnd('.', '!')

                if (subject.length < 3 || objectTerm.length < 3) continue
                if (isStopWord(subject)) continue

                val key = "${subject.lowercase()}:${objectTerm.take(20).lowercase()}"
                if (key in seen) continue
                seen.add(key)

                results.add(
                    ExtractedRelationship(
                        subject        = subject,
                        predicate      = pattern.predicate,
                        objectTerm     = objectTerm,
                        sourceSentence = sentence
                    )
                )
                break
            }
        }
        return results
    }

    private fun isStopWord(term: String): Boolean {
        val lower = term.lowercase().trim()
        return lower in setOf("it", "this", "that", "they", "there", "which", "what", "each", "all", "both")
    }
}
