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
import kotlinx.coroutines.tasks.await

class VisionBoardViewModel : ViewModel() {

    private val db     = FirebaseFirestore.getInstance()
    private val auth   = FirebaseAuth.getInstance()
    private val userId get() = auth.currentUser?.uid ?: "mock_user_id"

    private val _goalText        = MutableStateFlow("")
    val goalText: StateFlow<String> = _goalText.asStateFlow()

    private val _selectedEmoji   = MutableStateFlow("🏅")
    val selectedEmoji: StateFlow<String> = _selectedEmoji.asStateFlow()

    private val _targetValue     = MutableStateFlow("")
    val targetValue: StateFlow<String> = _targetValue.asStateFlow()

    private val _selectedSubject = MutableStateFlow("General")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _subjects = MutableStateFlow(listOf("General", "Biology", "Physics", "Math"))
    val subjects: StateFlow<List<String>> = _subjects.asStateFlow()

    private val _pinnedGoals     = MutableStateFlow<List<VisionGoalModel>>(emptyList())
    val pinnedGoals: StateFlow<List<VisionGoalModel>> = _pinnedGoals.asStateFlow()

    // For edit dialog state
    private val _editingGoal = MutableStateFlow<VisionGoalModel?>(null)
    val editingGoal: StateFlow<VisionGoalModel?> = _editingGoal.asStateFlow()

    fun setGoalText(text: String)      { _goalText.value = text }
    fun setEmoji(emoji: String)        { _selectedEmoji.value = emoji }
    fun setTargetValue(value: String)  { _targetValue.value = value }
    fun selectSubject(subject: String) { _selectedSubject.value = subject }

    fun addSubject(subject: String) {
        if (subject.isNotBlank() && !_subjects.value.contains(subject)) {
            _subjects.value = _subjects.value + subject
        }
    }

    fun startEditing(goal: VisionGoalModel) { _editingGoal.value = goal }
    fun stopEditing()                       { _editingGoal.value = null }

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val uid = firebaseAuth.currentUser?.uid ?: "mock_user_id"
            listenToGoals(uid)
        }
        // Also trigger it immediately in case listener is slow
        listenToGoals(userId)
    }

    private fun listenToGoals(uid: String) {
        db.collection("users").document(uid).collection("visionBoard")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("VisionBoard", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _pinnedGoals.value = snapshot.documents.mapNotNull { doc ->
                        VisionGoalModel(
                            id          = doc.id,
                            userId      = uid,
                            text        = doc.getString("text")        ?: return@mapNotNull null,
                            emoji       = doc.getString("emoji")       ?: "🏅",
                            targetValue = doc.getString("targetValue") ?: "",
                            subject     = doc.getString("subject")     ?: "General"
                        )
                    }
                }
            }
    }

    fun pinGoal(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val currentText = _goalText.value.trim()
        val currentUid = userId

        if (currentText.isBlank()) {
            onComplete(false, "Goal text is empty")
            return
        }
        
        val data = hashMapOf(
            "text"        to currentText,
            "emoji"       to _selectedEmoji.value,
            "targetValue" to _targetValue.value.trim(),
            "subject"     to _selectedSubject.value,
            "createdAt"   to System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                db.collection("users").document(currentUid)
                    .collection("visionBoard").add(data).await()
                
                _goalText.value    = ""
                _targetValue.value = ""
                onComplete(true, "Pinned successfully!")
            } catch (e: Exception) { 
                android.util.Log.e("VisionBoard", "Error adding goal", e)
                onComplete(false, "Failed to pin: ${e.message}")
            }
        }
    }

    fun editGoal(goal: VisionGoalModel) {
        if (userId.isEmpty() || goal.id.isEmpty()) return
        val data = hashMapOf(
            "text"        to goal.text.trim(),
            "emoji"       to goal.emoji,
            "targetValue" to goal.targetValue.trim(),
            "subject"     to goal.subject
        )
        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .collection("visionBoard").document(goal.id)
                    .update(data as Map<String, Any>).await()
                _editingGoal.value = null  // close edit dialog after save
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun removeGoal(goal: VisionGoalModel) {
        if (userId.isEmpty() || goal.id.isEmpty()) return
        viewModelScope.launch {
            try {
                db.collection("users").document(userId)
                    .collection("visionBoard").document(goal.id).delete().await()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}