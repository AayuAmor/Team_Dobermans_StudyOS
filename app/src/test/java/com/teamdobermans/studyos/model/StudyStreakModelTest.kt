package com.teamdobermans.studyos.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StudyStreakModelTest {

    @Test
    fun `first activity creates a streak of one`() {
        val result = StudyStreakModel.calculateNextStreak(
            currentStreak = 0,
            longestStreak = 0,
            totalStudyDays = 0,
            lastStudyDate = null,
            today = LocalDate.of(2026, 7, 2)
        )

        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
        assertEquals(1, result.totalStudyDays)
        assertEquals("2026-07-02", result.lastStudyDate)
    }

    @Test
    fun `same day activity does not double increment`() {
        val result = StudyStreakModel.calculateNextStreak(
            currentStreak = 3,
            longestStreak = 5,
            totalStudyDays = 5,
            lastStudyDate = "2026-07-02",
            today = LocalDate.of(2026, 7, 2)
        )

        assertEquals(3, result.currentStreak)
        assertEquals(5, result.longestStreak)
        assertEquals(5, result.totalStudyDays)
        assertEquals("2026-07-02", result.lastStudyDate)
    }

    @Test
    fun `next day increases streak by one`() {
        val result = StudyStreakModel.calculateNextStreak(
            currentStreak = 2,
            longestStreak = 2,
            totalStudyDays = 2,
            lastStudyDate = "2026-07-01",
            today = LocalDate.of(2026, 7, 2)
        )

        assertEquals(3, result.currentStreak)
        assertEquals(3, result.longestStreak)
        assertEquals(3, result.totalStudyDays)
    }

    @Test
    fun `missed day resets current streak`() {
        val result = StudyStreakModel.calculateNextStreak(
            currentStreak = 2,
            longestStreak = 5,
            totalStudyDays = 5,
            lastStudyDate = "2026-06-28",
            today = LocalDate.of(2026, 7, 2)
        )

        assertEquals(1, result.currentStreak)
        assertEquals(5, result.longestStreak)
        assertEquals(6, result.totalStudyDays)
    }
}
