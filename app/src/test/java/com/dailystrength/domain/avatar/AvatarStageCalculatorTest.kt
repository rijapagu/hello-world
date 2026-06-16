package com.dailystrength.domain.avatar

import com.dailystrength.domain.model.AvatarStage
import com.dailystrength.domain.model.Streak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AvatarStageCalculatorTest {

    @Test
    fun `seed stage at zero`() {
        assertEquals(AvatarStage.SEED, AvatarStageCalculator.stageFor(Streak.EMPTY))
    }

    @Test
    fun `crossing milestones unlocks higher stages`() {
        assertEquals(AvatarStage.SPARK, AvatarStageCalculator.stageFor(streak(7)))
        assertEquals(AvatarStage.FORGED, AvatarStageCalculator.stageFor(streak(30)))
        assertEquals(AvatarStage.LEGEND, AvatarStageCalculator.stageFor(streak(400)))
    }

    @Test
    fun `stage is based on longest streak so a missed day does not regress the avatar`() {
        val streak = Streak(currentStreak = 0, longestStreak = 60, lastCompletedEpochDay = 100, totalWorkouts = 60)
        assertEquals(AvatarStage.TEMPERED, AvatarStageCalculator.stageFor(streak))
    }

    @Test
    fun `progress to next is within bounds`() {
        val p = AvatarStageCalculator.progressToNext(streak(15))
        assertTrue(p in 0f..1f)
    }

    private fun streak(longest: Int) =
        Streak(currentStreak = longest, longestStreak = longest, lastCompletedEpochDay = 100, totalWorkouts = longest)
}
