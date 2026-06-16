package com.dailystrength.domain.model

/**
 * Avatar evolution stage, driven purely by consistency (streak milestones) — never by body weight,
 * calories or photos. Each stage maps to visual parameters consumed by the 3D renderer.
 *
 * @param minStreakDays the streak (or longest streak) threshold to unlock this stage.
 * @param posture 0f..1f straighter posture as the user evolves.
 * @param definition 0f..1f muscular definition.
 * @param athleticism 0f..1f overall athletic appearance.
 */
enum class AvatarStage(
    val milestoneDays: Int,
    val displayName: String,
    val posture: Float,
    val definition: Float,
    val athleticism: Float,
) {
    SEED(0, "Inicio", 0.20f, 0.10f, 0.10f),
    SPARK(7, "Chispa", 0.35f, 0.25f, 0.25f),
    FORGED(30, "Forjado", 0.55f, 0.45f, 0.45f),
    TEMPERED(60, "Templado", 0.70f, 0.60f, 0.60f),
    HARDENED(90, "Endurecido", 0.82f, 0.72f, 0.74f),
    RELENTLESS(180, "Implacable", 0.92f, 0.86f, 0.88f),
    LEGEND(365, "Leyenda", 1.0f, 1.0f, 1.0f);

    companion object {
        /** Ordered milestone thresholds, for progress bars between stages. */
        val milestones: List<Int> get() = entries.map { it.milestoneDays }
    }
}
