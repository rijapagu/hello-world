package com.dailystrength.di

import com.dailystrength.data.repository.AiCoachRepositoryImpl
import com.dailystrength.data.repository.ExerciseRepositoryImpl
import com.dailystrength.data.repository.ProgressRepositoryImpl
import com.dailystrength.data.repository.StreakRepositoryImpl
import com.dailystrength.data.repository.UserRepositoryImpl
import com.dailystrength.data.repository.WorkoutRepositoryImpl
import com.dailystrength.domain.repository.AiCoachRepository
import com.dailystrength.domain.repository.ExerciseRepository
import com.dailystrength.domain.repository.ProgressRepository
import com.dailystrength.domain.repository.StreakRepository
import com.dailystrength.domain.repository.UserRepository
import com.dailystrength.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository

    @Binds @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds @Singleton
    abstract fun bindAiCoachRepository(impl: AiCoachRepositoryImpl): AiCoachRepository
}
