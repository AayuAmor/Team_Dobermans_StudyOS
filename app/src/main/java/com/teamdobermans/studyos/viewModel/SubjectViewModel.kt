package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.SubjectModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubjectViewModel : ViewModel() {

    private val _subjects = MutableStateFlow(
        listOf(
            SubjectModel("sub_gen",     "General Study"),
            SubjectModel("sub_cs_101",  "Computer Architecture"),
            SubjectModel("sub_math_3",  "Linear Algebra"),
            SubjectModel("sub_engcomp", "Advanced Technical Writing")
        )
    )
    val subjects: StateFlow<List<SubjectModel>> = _subjects.asStateFlow()

    fun addSubject(name: String) {
        val id = "sub${System.currentTimeMillis()}"
        _subjects.value = _subjects.value + SubjectModel(id, name)
    }
}

