package com.teamdobermans.studyos.assessment.analyze

import com.teamdobermans.studyos.assessment.model.ExtractedProcess
import com.teamdobermans.studyos.assessment.model.NoteAnalysis

class NoteAnalyzer(
    private val definitionDetector: DefinitionDetector = DefinitionDetector(),
    private val conceptExtractor: ConceptExtractor = ConceptExtractor(),
    private val relationshipDetector: RelationshipDetector = RelationshipDetector()
) {
    fun analyze(title: String, body: String): NoteAnalysis {
        val sentences = splitSentences(body)
        return NoteAnalysis(
            noteTitle     = title,
            rawText       = body,
            sentences     = sentences,
            definitions   = definitionDetector.detect(sentences),
            concepts      = conceptExtractor.extract(body, sentences),
            relationships = relationshipDetector.detect(sentences),
            processes     = detectProcesses(sentences)
        )
    }

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+"))
            .flatMap { chunk -> chunk.split(Regex("\n+")) }
            .map { it.trim() }
            .filter { s -> s.length > 10 && s.split(Regex("\\s+")).size >= 4 }

    private fun detectProcesses(sentences: List<String>): List<ExtractedProcess> {
        val sequenceMarkers = listOf("first", "second", "third", "then", "next", "finally", "lastly", "after", "before", "step")
        val stepSentences = sentences.filter { sentence ->
            sequenceMarkers.any { marker ->
                sentence.lowercase().contains(Regex("\\b$marker\\b"))
            }
        }
        if (stepSentences.size < 2) return emptyList()
        return listOf(ExtractedProcess(name = "Process", steps = stepSentences))
    }
}
