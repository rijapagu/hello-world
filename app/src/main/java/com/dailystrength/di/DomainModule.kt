package com.dailystrength.di

import com.dailystrength.domain.util.DateProvider
import com.dailystrength.domain.util.SystemDateProvider
import com.dailystrength.domain.workout.WorkoutGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides @Singleton
    fun provideWorkoutGenerator(): WorkoutGenerator = WorkoutGenerator()

    @Provides @Singleton
    fun provideDateProvider(): DateProvider = SystemDateProvider()
}
