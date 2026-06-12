package com.teamdobermans.studyos.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.VisionGoalModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val studyStreak: Int        = 15,
    val weeklyHours: Float      = 8.5f,
    val tasksCompleted: Int     = 3,
    val totalTasks: Int         = 7,
    val dailyGoalPercent: Float = 0.60f,
    val lastSubject: String     = "Software Development",
    val lastNote: String        = "Design Patterns",
    val flashcardsDueToday: Int = 12,
    val todayStudyMinutes: Int  = 95,
    val goals: List<VisionGoalModel> = emptyList() // CHANGED from List<String>
)

class HomeViewModel : ViewModel() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        listenToVisionGoals()
    }

    private fun listenToVisionGoals() {
        if (userId.isEmpty()) return
        db.collection("users").document(userId)
            .collection("visionBoard")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val goals = snapshot.documents.mapNotNull { doc ->
                    VisionGoalModel(
                        id          = doc.id,
                        userId      = userId,
                        text        = doc.getString("text")        ?: return@mapNotNull null,
                        emoji       = doc.getString("emoji")       ?: "🏅",
                        targetValue = doc.getString("targetValue") ?: "",
                        subject     = doc.getString("subject")     ?: "General"
                    )
                }
                _state.value = _state.value.copy(goals = goals)
            }
    }
}