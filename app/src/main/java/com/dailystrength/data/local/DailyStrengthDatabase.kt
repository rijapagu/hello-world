package com.dailystrength.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dailystrength.data.local.dao.ExerciseDao
import com.dailystrength.data.local.dao.ProgressDao
import com.dailystrength.data.local.dao.StreakDao
import com.dailystrength.data.local.dao.UserProfileDao
import com.dailystrength.data.local.dao.WorkoutDao
import com.dailystrength.data.local.entity.CompletedSetEntity
import com.dailystrength.data.local.entity.ExerciseEntity
import com.dailystrength.data.local.entity.ProgressPointEntity
import com.dailystrength.data.local.entity.StreakEntity
import com.dailystrength.data.local.entity.UserProfileEntity
import com.dailystrength.data.local.entity.WorkoutEntity
import com.dailystrength.data.local.entity.WorkoutExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        UserProfileEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        CompletedSetEntity::class,
        StreakEntity::class,
        ProgressPointEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DailyStrengthDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun streakDao(): StreakDao
    abstract fun progressDao(): ProgressDao

    companion object {
        const val NAME = "daily_strength.db"
    }
}
