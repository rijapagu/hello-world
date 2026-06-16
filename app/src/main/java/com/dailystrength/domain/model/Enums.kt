package com.dailystrength.domain.model

/** User self-reported fitness level. Drives volume and exercise difficulty selection. */
enum class FitnessLevel(val baseSets: Int, val difficultyCeiling: Int) {
    BEGINNER(baseSets = 2, difficultyCeiling = 1),
    INTERMEDIATE(baseSets = 3, difficultyCeiling = 2),
    ADVANCED(baseSets = 4, difficultyCeiling = 3),
}

/** Equipment a user can own. An exercise is selectable only if its required equipment is a subset. */
enum class Equipment {
    PULL_UP_BAR,
    DIP_STATION,
    RESISTANCE_BANDS,
    AB_WHEEL,
    NONE, // bodyweight, always available
}

/** Training categories. Calisthenics-focused, no gym split. */
enum class ExerciseCategory {
    PULL,
    PUSH,
    LEGS,
    CORE,
    MOBILITY,
    RECOVERY,
}

/** Primary target muscles, used for progression analytics and balance. */
enum class MuscleGroup {
    LATS, BICEPS, FOREARMS, UPPER_BACK, REAR_DELTS,
    CHEST, TRICEPS, FRONT_DELTS, SHOULDERS,
    QUADS, HAMSTRINGS, GLUTES, CALVES,
    ABS, OBLIQUES, LOWER_BACK,
    HIPS, ANKLES, FULL_BODY,
}

/** Exercise difficulty 1..3, compared against FitnessLevel.difficultyCeiling (+1 tolerance). */
enum class Difficulty(val level: Int) {
    EASY(1),
    MODERATE(2),
    HARD(3),
}

/** Sport played today, reported by the user (or detected via Samsung Health). Adapts the plan. */
enum class SportContext {
    NONE,
    TENNIS,
    PADEL;

    /** Tennis and padel both pre-fatigue legs and one shoulder; we reduce lower-body load. */
    val reducesLowerBody: Boolean get() = this == TENNIS || this == PADEL
}

/** Lifecycle of a generated workout. */
enum class WorkoutStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
}

/** Where the workout plan came from. Rule engine is the offline-first fallback. */
enum class WorkoutSource {
    AI,
    RULE_ENGINE,
}
