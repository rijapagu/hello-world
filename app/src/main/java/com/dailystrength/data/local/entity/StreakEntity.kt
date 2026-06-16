package com.dailystrength.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row denormalized streak table for O(1) widget reads. Always id = 0. */
@Entity(tableName = "streak")
data class StreakEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletedEpochDay: Long?,
    val totalWorkouts: Int,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
