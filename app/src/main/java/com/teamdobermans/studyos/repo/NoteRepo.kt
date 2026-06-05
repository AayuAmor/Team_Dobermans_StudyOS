package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.NoteModel

interface NoteRepo {

    fun addNote(

        model: NoteModel,

        callback:(Boolean,String)->Unit
    )
}
