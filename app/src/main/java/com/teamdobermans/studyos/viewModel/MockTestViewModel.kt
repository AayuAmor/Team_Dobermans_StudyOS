package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import com.teamdobermans.studyos.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockTestViewModel : ViewModel() {

    private val _subjects        = MutableStateFlow(listOf("All Subjects"))
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val _selectedSubject = MutableStateFlow("All Subjects")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _durationIndex   = MutableStateFlow(1)
    val durationIndex: StateFlow<Int> = _durationIndex.asStateFlow()

    private val _questionIndex   = MutableStateFlow(1)
    val questionIndex: StateFlow<Int> = _questionIndex.asStateFlow()

    private val _difficulty      = MutableStateFlow(Difficulty.MEDIUM)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    fun selectSubject(subject: String) { _selectedSubject.value = subject }
    fun addSubject(name: String)        { if (name.isNotBlank()) _subjects.value = _subjects.value + name }
    fun setDurationIndex(i: Int)        { _durationIndex.value = i }
    fun setQuestionIndex(i: Int)        { _questionIndex.value = i }
    fun setDifficulty(d: Difficulty)    { _difficulty.value = d }
}

