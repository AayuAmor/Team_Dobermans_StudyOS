package com.teamdobermans.studyos.assessment.analyze

import com.teamdobermans.studyos.assessment.model.ExtractedDefinition

class DefinitionDetector {

    private data class DetectionPattern(val regex: Regex)

    private val patterns = listOf(
        DetectionPattern(Regex("""^(.{2,45}?)\s+(?:is|are)\s+(?:a|an|the)\s+(.{5,})""", RegexOption.IGNORE_CASE)),
        DetectionPattern(Regex("""^(.{2,45}?)\s+is\s+defined\s+as\s+(.{5,})""", RegexOption.IGNORE_CASE)),
        DetectionPattern(Regex("""^(.{2,45}?)\s+(?:refers?\s+to|stands?\s+for|denotes?)\s+(.{5,})""", RegexOption.IGNORE_CASE)),
        DetectionPattern(Regex("""^(.{2,45}?)\s+means?\s+(.{5,})""", RegexOption.IGNORE_CASE)),
        DetectionPattern(Regex("""^([A-Z][^:]{1,40}):\s+(.{10,})""")),
        DetectionPattern(Regex("""^(.{2,45}?)\s+is\s+used\s+(?:to|for)\s+(.{5,})""", RegexOption.IGNORE_CASE)),
        DetectionPattern(Regex("""^(.{2,45}?)\s+(?:can\s+be\s+described|can\s+be\s+defined)\s+as\s+(.{5,})""", RegexOption.IGNORE_CASE))
    )

    fun detect(sentences: List<String>): List<ExtractedDefinition> {
        val results = mutableListOf<ExtractedDefinition>()
        val seenTerms = mutableSetOf<String>()

        for (sentence in sentences) {
            val cleaned = sentence.trimEnd('.', '!', '?')
            for (pattern in patterns) {
                val match = pattern.regex.find(cleaned) ?: continue
                val rawTerm = match.groupValues[1].trim()
                val rawDef  = match.groupValues[2].trim().trimEnd('.', '!')

                if (rawTerm.length < 2 || rawTerm.length > 50) continue
                if (rawDef.length < 5) continue
                if (rawTerm.lowercase() in seenTerms) continue
                if (isStopPhrase(rawTerm)) continue

                val term = normalizeTerm(rawTerm)
                if (term.isBlank()) continue
                seenTerms.add(term.lowercase())

                results.add(
                    ExtractedDefinition(
                        term           = term,
                        definition     = rawDef,
                        sourceSentence = sentence
                    )
                )
                break
            }
        }
        return results
    }

    private fun normalizeTerm(raw: String): String =
        raw.trim()
            .replace(Regex("^(?:a|an|the)\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
            .replaceFirstChar { it.uppercaseChar() }

    private fun isStopPhrase(term: String): Boolean {
        val lower = term.lowercase().trim()
        return lower in STOP_PHRASES
            || lower.length < 2
            || lower.split(Regex("\\s+")).size > 5
            || lower.all { !it.isLetter() }
    }

    companion object {
        private val STOP_PHRASES = setOf(
            "it", "this", "that", "they", "there", "here",
            "he", "she", "we", "you", "i", "which", "what",
            "where", "when", "why", "how", "each", "all",
            "both", "some", "any", "other", "another"
        )
    }
}
