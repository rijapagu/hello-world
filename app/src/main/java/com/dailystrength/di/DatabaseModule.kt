package com.dailystrength.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dailystrength.data.local.DailyStrengthDatabase
import com.dailystrength.data.local.dao.ExerciseDao
import com.dailystrength.data.local.dao.ProgressDao
import com.dailystrength.data.local.dao.StreakDao
import com.dailystrength.data.local.dao.UserProfileDao
import com.dailystrength.data.local.dao.WorkoutDao
import com.dailystrength.data.local.seed.ExerciseSeed
import com.dailystrength.data.mapper.toEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "daily_strength_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        exerciseDaoProvider: Provider<ExerciseDao>,
    ): DailyStrengthDatabase {
        // Application-scoped seeding scope; the seed runs once on database creation.
        val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return Room.databaseBuilder(context, DailyStrengthDatabase::class.java, DailyStrengthDatabase.NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedScope.launch {
                        exerciseDaoProvider.get().upsertAll(ExerciseSeed.exercises.map { it.toEntity() })
                    }
                }
            })
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides fun provideExerciseDao(db: DailyStrengthDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun provideUserProfileDao(db: DailyStrengthDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun provideWorkoutDao(db: DailyStrengthDatabase): WorkoutDao = db.workoutDao()
    @Provides fun provideStreakDao(db: DailyStrengthDatabase): StreakDao = db.streakDao()
    @Provides fun provideProgressDao(db: DailyStrengthDatabase): ProgressDao = db.progressDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
