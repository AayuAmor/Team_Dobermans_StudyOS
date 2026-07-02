package com.teamdobermans.studyos.repo

import com.teamdobermans.studyos.model.BrainGameMode
import com.teamdobermans.studyos.model.BrainGameScoreModel

interface BrainGameRepository {
    suspend fun saveScore(score: BrainGameScoreModel): Result<Unit>
    suspend fun getBestScore(mode: BrainGameMode): Result<BrainGameScoreModel?>
    suspend fun getRecentScores(limit: Int = 10): Result<List<BrainGameScoreModel>>
}
