package com.teamdobermans.studyos.assessment.generate

import com.teamdobermans.studyos.assessment.model.NoteAnalysis
import com.teamdobermans.studyos.assessment.model.QuestionBlueprint
import com.teamdobermans.studyos.assessment.model.QuestionType
import com.teamdobermans.studyos.model.Difficulty

interface QuestionGenerator {
    val supportedTypes: List<QuestionType>
    fun canGenerate(analysis: NoteAnalysis): Boolean
    fun generate(analysis: NoteAnalysis, difficulty: Difficulty): List<QuestionBlueprint>
}
