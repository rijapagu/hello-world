package com.dailystrength.domain.avatar

import com.dailystrength.domain.model.AvatarStage
import com.dailystrength.domain.model.Streak

/**
 * Maps consistency (streaks) to an [AvatarStage]. Evolution is driven by the *longest* streak so the
 * avatar never visually regresses after a single missed day — that would punish exactly the moment
 * the user needs encouragement (Never Zero).
 */
object AvatarStageCalculator {

    fun stageFor(streak: Streak): AvatarStage {
        val basis = maxOf(streak.longestStreak, streak.currentStreak)
        return AvatarStage.entries
            .filter { basis >= it.milestoneDays }
            .maxByOrNull { it.milestoneDays }
            ?: AvatarStage.SEED
    }

    /** The next stage to unlock, or null if already at the top. */
    fun nextStage(streak: Streak): AvatarStage? {
        val current = stageFor(streak)
        return AvatarStage.entries
            .filter { it.milestoneDays > current.milestoneDays }
            .minByOrNull { it.milestoneDays }
    }

    /** Progress in 0f..1f toward the next milestone, for the dashboard ring. */
    fun progressToNext(streak: Streak): Float {
        val basis = maxOf(streak.longestStreak, streak.currentStreak)
        val current = stageFor(streak)
        val next = nextStage(streak) ?: return 1f
        val span = (next.milestoneDays - current.milestoneDays).toFloat()
        if (span <= 0f) return 1f
        return ((basis - current.milestoneDays) / span).coerceIn(0f, 1f)
    }
}
