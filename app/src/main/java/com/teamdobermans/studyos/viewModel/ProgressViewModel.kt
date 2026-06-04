package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.SubjectProgressModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class ProgressViewModel : ViewModel() {

    private val _subjectProgress = MutableStateFlow(
        listOf(
            SubjectProgressModel("Science", 0.40f, 0xFF6C5DD3.toInt()),
            SubjectProgressModel("Social",  0.45f, 0xFFE53935.toInt()),
            SubjectProgressModel("Math",    0.75f, 0xFFBFA300.toInt())
        )
    )
    val subjectProgress: StateFlow<List<SubjectProgressModel>> = _subjectProgress.asStateFlow()

    val heatData: Array<IntArray> = Array(4) { IntArray(36) { Random.nextInt(0, 5) } }
}
