package com.teamdobermans.studyos

import androidx.compose.runtime.mutableStateListOf


data class NoteSubject(val id: String, val name: String)

object StudyDataRepository {

    val dynamicSubjects = mutableStateListOf(
        NoteSubject("sub_gen", "General Study"),
        NoteSubject("sub_cs_101", "Computer Architecture"),
        NoteSubject("sub_math_3", "Linear Algebra"),
        NoteSubject("sub_engcomp", "Advanced Technical Writing")
    )

    fun addNewSubjectFromNotes(name: String) {
        val uniqueId = "sub${System.currentTimeMillis()}"
        dynamicSubjects.add(NoteSubject(uniqueId, name))
    }
}