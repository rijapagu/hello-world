package com.dailystrength.domain.model

/**
 * The user's profile. There is exactly one row (single-user app); persisted locally and synced
 * to the account identified by [googleId].
 */
data class UserProfile(
    val name: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val fitnessLevel: FitnessLevel,
    val equipment: Set<Equipment>,
    val googleId: String? = null,
    val avatarId: String? = null, // Ready Player Me avatar id (Phase 1)
) {
    /** Equipment usable for selection, always including bodyweight. */
    val usableEquipment: Set<Equipment> get() = equipment + Equipment.NONE

    companion object {
        /** Safe default used before onboarding completes; guarantees Never-Zero generation works. */
        val DEFAULT = UserProfile(
            name = "",
            age = 30,
            heightCm = 175,
            weightKg = 75.0,
            fitnessLevel = FitnessLevel.BEGINNER,
            equipment = setOf(Equipment.NONE),
        )
    }
}
