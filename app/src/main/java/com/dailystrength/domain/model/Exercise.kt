package com.dailystrength.domain.model

/**
 * A single exercise in the internal library. This is the ONLY source of truth the AI Coach may
 * select from — the LLM never invents exercises, it only references [id] values that exist here.
 *
 * @param id stable slug, e.g. "assisted_pullup". Used as the contract with the AI and across versions.
 * @param defaultRepsByLevel base target reps per [FitnessLevel] for rep-based exercises.
 * @param defaultHoldSeconds base hold duration for isometric exercises (planks, hangs); null if rep-based.
 * @param isUnilateral true if performed per-side (split squats, side planks) — affects volume display.
 * @param animationRef reference to the 3D animation asset (start → movement → end). May be null in Phase 0.
 */
data class Exercise(
    val id: String,
    val name: String,
    val category: ExerciseCategory,
    val difficulty: Difficulty,
    val description: String,
    val instructions: List<String>,
    val commonMistakes: List<String>,
    val requiredEquipment: Set<Equipment>,
    val targetMuscles: Set<MuscleGroup>,
    val videoUrl: String?,
    val animationRef: String?,
    val isUnilateral: Boolean = false,
    val defaultRepsByLevel: Map<FitnessLevel, Int> = emptyMap(),
    val defaultHoldSeconds: Int? = null,
) {
    val isHold: Boolean get() = defaultHoldSeconds != null

    /** Whether this exercise can be performed with the given equipment set. */
    fun isAvailableWith(owned: Set<Equipment>): Boolean {
        val needs = requiredEquipment - Equipment.NONE
        return needs.isEmpty() || owned.containsAll(needs)
    }

    fun targetRepsFor(level: FitnessLevel): Int =
        defaultRepsByLevel[level] ?: defaultRepsByLevel.values.firstOrNull() ?: 8
}
