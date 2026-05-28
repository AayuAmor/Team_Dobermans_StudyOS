package com.teamdobermans.studyos.repo

import com.google.firebase.database.FirebaseDatabase
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.repo.NoteRepo

class NoteRepoImpl : NoteRepo {

    private val database = FirebaseDatabase.getInstance()

    private val ref = database.getReference("notes")

    override fun addNote(
        model: NoteModel,
        callback: (Boolean, String) -> Unit
    ) {

        val id = ref.push().key.toString()

        model.id = id

        ref.child(id).setValue(model)
            .addOnCompleteListener {

                if (it.isSuccessful) {

                    callback(true, "Note Added")

                } else {

                    callback(
                        false,
                        it.exception?.message ?: "Error occurred"
                    )
                }
            }
    }
}