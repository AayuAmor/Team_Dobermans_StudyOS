package com.teamdobermans.studyos.assessment.model

import com.teamdobermans.studyos.model.Difficulty

enum class QuestionType(val label: String) {
    DEFINITION("Definition"),
    CONCEPT_RECOGNITION("Concept Recognition"),
    FILL_IN_BLANK("Fill in the Blank"),
    RELATIONSHIP("Relationship"),
    PROCESS("Process"),
    APPLICATION("Application")
}

data class ExtractedDefinition(
    val term: String,
    val definition: String,
    val sourceSentence: String
)

data class ExtractedConcept(
    val term: String,
    val frequency: Int,
    val firstContext: String,
    val importance: Float
)

data class ExtractedRelationship(
    val subject: String,
    val predicate: String,
    val objectTerm: String,
    val sourceSentence: String
)

data class ExtractedProcess(
    val name: String,
    val steps: List<String>
)

data class NoteAnalysis(
    val noteTitle: String,
    val rawText: String,
    val sentences: List<String>,
    val definitions: List<ExtractedDefinition>,
    val concepts: List<ExtractedConcept>,
    val relationships: List<ExtractedRelationship>,
    val processes: List<ExtractedProcess>
) {
    val allTerms: List<String>
        get() = (definitions.map { it.term } + concepts.map { it.term }).distinct()

    val isSubstantial: Boolean
        get() = sentences.size >= 3 && (definitions.size >= 2 || concepts.size >= 4)
}

data class QuestionBlueprint(
    val type: QuestionType,
    val stem: String,
    val correctAnswer: String,
    val distractorPool: List<String>,
    val sourceSentence: String,
    val sourceConcept: String,
    val difficulty: Difficulty,
    val confidenceScore: Float
)
