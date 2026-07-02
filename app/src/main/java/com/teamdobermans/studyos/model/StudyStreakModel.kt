package com.teamdobermans.studyos.model

import java.time.LocalDate

data class StudyStreakModel(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val lastStreakUpdatedAt: Long = 0L,
    val totalStudyDays: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun calculateNextStreak(
            currentStreak: Int,
            longestStreak: Int,
            totalStudyDays: Int,
            lastStudyDate: String?,
            today: LocalDate
        ): StudyStreakModel {
            val todayString = today.toString()
            val yesterdayString = today.minusDays(1).toString()

            if (lastStudyDate == null) {
                return StudyStreakModel(
                    currentStreak = 1,
                    longestStreak = 1,
                    lastStudyDate = todayString,
                    totalStudyDays = 1,
                    updatedAt = System.currentTimeMillis()
                )
            }

            if (lastStudyDate == todayString) {
                return StudyStreakModel(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastStudyDate = todayString,
                    totalStudyDays = totalStudyDays,
                    updatedAt = System.currentTimeMillis()
                )
            }

            if (lastStudyDate == yesterdayString) {
                val nextCurrent = currentStreak + 1
                return StudyStreakModel(
                    currentStreak = nextCurrent,
                    longestStreak = maxOf(longestStreak, nextCurrent),
                    lastStudyDate = todayString,
                    totalStudyDays = totalStudyDays + 1,
                    updatedAt = System.currentTimeMillis()
                )
            }

            return StudyStreakModel(
                currentStreak = 1,
                longestStreak = maxOf(longestStreak, 1),
                lastStudyDate = todayString,
                totalStudyDays = totalStudyDays + 1,
                updatedAt = System.currentTimeMillis()
            )
        }

        fun resetIfMissed(
            currentStreak: Int,
            longestStreak: Int,
            lastStudyDate: String?,
            totalStudyDays: Int,
            today: LocalDate
        ): StudyStreakModel {
            if (lastStudyDate == null) {
                return StudyStreakModel(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastStudyDate = null,
                    totalStudyDays = totalStudyDays,
                    updatedAt = System.currentTimeMillis()
                )
            }

            val todayString = today.toString()
            val yesterdayString = today.minusDays(1).toString()
            if (lastStudyDate == todayString || lastStudyDate == yesterdayString) {
                return StudyStreakModel(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    lastStudyDate = lastStudyDate,
                    totalStudyDays = totalStudyDays,
                    updatedAt = System.currentTimeMillis()
                )
            }

            return StudyStreakModel(
                currentStreak = 0,
                longestStreak = longestStreak,
                lastStudyDate = lastStudyDate,
                totalStudyDays = totalStudyDays,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
