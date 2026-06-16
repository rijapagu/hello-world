package com.dailystrength.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.FitnessLevel

/** Single-row table: there is exactly one user profile, always at id = 0. */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val name: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val fitnessLevel: FitnessLevel,
    val equipment: Set<Equipment>,
    val googleId: String?,
    val avatarId: String?,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
