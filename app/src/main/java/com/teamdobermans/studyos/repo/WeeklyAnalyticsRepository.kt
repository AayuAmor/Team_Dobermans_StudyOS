package com.teamdobermans.studyos.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamdobermans.studyos.model.FocusSessionModel
import com.teamdobermans.studyos.model.NoteModel
import com.teamdobermans.studyos.model.QuizAttemptModel
import com.teamdobermans.studyos.model.WeeklyTrendPoint
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class WeeklyAnalyticsRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val dayMs = 24L * 60 * 60 * 1000

    data class DayRange(val label: String, val start: Long, val end: Long)

    fun currentWeekDays(): List<DayRange> {
        val cal = Calendar.getInstance().apply {
            val dow = get(Calendar.DAY_OF_WEEK)
            val daysBack = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").mapIndexed { i, label ->
            val start = cal.timeInMillis + i * dayMs
            DayRange(label, start, start + dayMs)
        }
    }

    private fun emptyWeek(): List<WeeklyTrendPoint> =
        currentWeekDays().map { WeeklyTrendPoint(it.label, it.start, 0f) }

    suspend fun getWeeklyStudyTime(): List<WeeklyTrendPoint> {
        val uid = auth.currentUser?.uid ?: return emptyWeek()
        val days = currentWeekDays()
        val weekStart = days.first().start
        val weekEnd = days.last().end
        return try {
            val sessions = firestore.collection("users").document(uid)
                .collection("focusSessions")
                .whereGreaterThanOrEqualTo("completedAt", weekStart)
                .whereLessThan("completedAt", weekEnd)
                .get().await()
                .documents.mapNotNull { it.toObject(FocusSessionModel::class.java) }
                .filter { it.completedAt > 0L }

            days.map { day ->
                val mins = sessions
                    .filter { it.completedAt >= day.start && it.completedAt < day.end }
                    .sumOf { it.durationMinutes }
                    .toFloat()
                WeeklyTrendPoint(day.label, day.start, mins)
            }
        } catch (_: Exception) {
            emptyWeek()
        }
    }

    suspend fun getWeeklyTasksCompleted(): List<WeeklyTrendPoint> {
        val days = currentWeekDays()
        val doneTasks = TaskRepository().getAllTasks().filter { it.done }
        val cal = Calendar.getInstance()
        return days.map { day ->
            val count = doneTasks.count { task ->
                try {
                    cal.set(
                        task.startDate.year,
                        task.startDate.monthValue - 1,
                        task.startDate.dayOfMonth,
                        0, 0, 0
                    )
                    cal.set(Calendar.MILLISECOND, 0)
                    val taskMs = cal.timeInMillis
                    taskMs >= day.start && taskMs < day.end
                } catch (_: Exception) {
                    false
                }
            }.toFloat()
            WeeklyTrendPoint(day.label, day.start, count)
        }
    }

    suspend fun getWeeklyQuizScores(): List<WeeklyTrendPoint> {
        val uid = auth.currentUser?.uid ?: return emptyWeek()
        val days = currentWeekDays()
        val weekStart = days.first().start
        val weekEnd = days.last().end
        return try {
            val attempts = firestore.collection("users").document(uid)
                .collection("quizAttempts")
                .whereGreaterThanOrEqualTo("completedAt", weekStart)
                .whereLessThan("completedAt", weekEnd)
                .get().await()
                .documents.mapNotNull { it.toObject(QuizAttemptModel::class.java) }
                .filter { it.completedAt > 0L }

            days.map { day ->
                val dayAttempts = attempts.filter { it.completedAt >= day.start && it.completedAt < day.end }
                val avg = if (dayAttempts.isEmpty()) 0f
                else dayAttempts.sumOf { it.scorePercentage.toDouble() }.toFloat() / dayAttempts.size
                WeeklyTrendPoint(day.label, day.start, avg)
            }
        } catch (_: Exception) {
            emptyWeek()
        }
    }

    suspend fun getWeeklyReviewCount(): List<WeeklyTrendPoint> {
        val uid = auth.currentUser?.uid ?: return emptyWeek()
        val days = currentWeekDays()
        val weekStart = days.first().start
        val weekEnd = days.last().end
        return try {
            val reviewedNotes = firestore.collection("notes")
                .whereEqualTo("userId", uid)
                .whereEqualTo("reminderEnabled", true)
                .get().await()
                .documents.mapNotNull { it.toObject(NoteModel::class.java) }
                .filter { it.lastReviewedAt in weekStart until weekEnd }

            days.map { day ->
                val count = reviewedNotes
                    .count { it.lastReviewedAt >= day.start && it.lastReviewedAt < day.end }
                    .toFloat()
                WeeklyTrendPoint(day.label, day.start, count)
            }
        } catch (_: Exception) {
            emptyWeek()
        }
    }
}
