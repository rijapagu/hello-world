package com.dailystrength.domain.model

/**
 * A daily aggregated progression sample for one exercise. Strength progression (best reps / volume)
 * is the metric the app tracks — not body weight, calories or photos.
 *
 * @param bestReps best single-set reps achieved that day (null for hold-based exercises).
 * @param bestHoldSeconds best hold achieved that day (null for rep-based exercises).
 * @param totalVolume sum of reps across all sets that day; proxy for work done.
 */
data class ProgressPoint(
    val exerciseId: String,
    val epochDay: Long,
    val bestReps: Int?,
    val bestHoldSeconds: Int?,
    val totalVolume: Int,
)

/** A named progression series shown on the stats dashboard (e.g. "Pull-Ups", "Push-Ups", "Dips"). */
data class ProgressSeries(
    val exerciseId: String,
    val label: String,
    val points: List<ProgressPoint>,
) {
    val latestBestReps: Int? get() = points.lastOrNull()?.bestReps
    val firstBestReps: Int? get() = points.firstOrNull()?.bestReps
}
