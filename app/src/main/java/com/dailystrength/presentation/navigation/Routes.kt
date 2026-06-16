package com.dailystrength.presentation.navigation

/** Type-safe-ish route definitions for the single-activity Compose navigation graph. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val STATS = "stats"

    const val WORKOUT = "workout/{workoutId}"
    fun workout(workoutId: Long) = "workout/$workoutId"

    const val WORKOUT_COMPLETE = "workout_complete/{streak}"
    fun workoutComplete(streak: Int) = "workout_complete/$streak"

    const val ARG_WORKOUT_ID = "workoutId"
    const val ARG_STREAK = "streak"
}
