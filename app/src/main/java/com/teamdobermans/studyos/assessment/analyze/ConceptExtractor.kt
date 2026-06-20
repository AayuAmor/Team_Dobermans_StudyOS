package com.teamdobermans.studyos.assessment.analyze

import com.teamdobermans.studyos.assessment.model.ExtractedConcept
import kotlin.math.min

class ConceptExtractor {

    fun extract(rawText: String, sentences: List<String>): List<ExtractedConcept> {
        val tokens = tokenize(rawText)
        val filtered = tokens.filter { isValidToken(it) }

        val frequencyMap = mutableMapOf<String, Int>()
        for (token in filtered) {
            frequencyMap[token.lowercase()] = (frequencyMap[token.lowercase()] ?: 0) + 1
        }

        val canonical = mutableMapOf<String, String>()
        for (token in filtered) {
            val key = token.lowercase()
            if (key !in canonical || (token[0].isUpperCase() && canonical[key]!![0].isLowerCase())) {
                canonical[key] = token
            }
        }

        val contextMap = buildContextMap(sentences, frequencyMap.keys)

        return frequencyMap.entries
            .mapNotNull { (lower, freq) ->
                val display  = canonical[lower] ?: lower.replaceFirstChar { it.uppercaseChar() }
                val context  = contextMap[lower] ?: ""
                val importance = computeImportance(display, freq, rawText.length)
                if (importance < 0.15f) null
                else ExtractedConcept(
                    term         = display,
                    frequency    = freq,
                    firstContext = context,
                    importance   = importance
                )
            }
            .sortedByDescending { it.importance }
            .take(25)
    }

    private fun tokenize(text: String): List<String> =
        text.split(Regex("[\\s,;()\\[\\]{}'\"\\n\\r]+"))
            .map { it.trim('.', '!', '?', ':', ';', ',', '(', ')') }
            .filter { it.isNotBlank() }

    private fun isValidToken(word: String): Boolean {
        if (word.length < 3) return false
        if (!word.any { it.isLetter() }) return false
        if (word.lowercase() in STOP_WORDS) return false
        return true
    }

    private fun buildContextMap(sentences: List<String>, terms: Set<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (sentence in sentences) {
            val lower = sentence.lowercase()
            for (term in terms) {
                if (term !in map && lower.contains(term)) {
                    map[term] = sentence
                }
            }
        }
        return map
    }

    private fun computeImportance(term: String, frequency: Int, textLength: Int): Float {
        var score = 0f
        score += min(frequency.toFloat(), 5f) / 5f * 0.4f
        if (term[0].isUpperCase()) score += 0.25f
        if (term.length in 4..20) score += 0.2f
        if (frequency >= 2) score += 0.15f
        return score
    }

    companion object {
        private val STOP_WORDS = setOf(
            "the","a","an","is","are","was","were","be","been","being",
            "have","has","had","do","does","did","will","would","could",
            "should","may","might","must","shall","can","need","ought",
            "in","on","at","by","for","with","about","against","between",
            "into","through","during","before","after","above","below",
            "from","up","down","of","off","over","under","then","once",
            "here","there","when","where","why","how","all","both","each",
            "few","more","most","other","some","such","not","only","same",
            "so","than","too","very","just","this","that","these","those",
            "and","or","but","if","as","what","which","who","whom",
            "they","we","you","he","she","him","her","his","our","my",
            "your","also","often","usually","now","always","never",
            "first","second","third","one","two","three","four","five",
            "many","much","every","any","either","neither","while",
            "since","because","although","however","therefore","thus",
            "used","use","using","uses","make","made","makes","making",
            "get","got","gets","set","sets","add","added","adds",
            "its","like","can","into","been","their","where","which",
            "each","both","such","even","only","also","still","yet",
            "well","back","give","given","way","case","type","form",
            "part","kind","sort","same","time","work","works","working",
            "when","then","next","last","more","less","new","old","own"
        )
    }
}
