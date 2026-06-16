package com.dailystrength.data.local.entity

import androidx.room.Entity
import androidx.room.Index

/** One progression sample per (exercise, day). Composite primary key keeps it idempotent. */
@Entity(
    tableName = "progress_point",
    primaryKeys = ["exerciseId", "epochDay"],
    indices = [Index("exerciseId")],
)
data class ProgressPointEntity(
    val exerciseId: String,
    val epochDay: Long,
    val bestReps: Int?,
    val bestHoldSeconds: Int?,
    val totalVolume: Int,
)
