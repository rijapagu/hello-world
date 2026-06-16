package com.dailystrength.di

import com.dailystrength.data.avatar.ReadyPlayerMeProvider
import com.dailystrength.data.health.NoopHealthDataSource
import com.dailystrength.domain.avatar.AvatarProvider
import com.dailystrength.domain.health.HealthDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds optional/enhancer integrations. Both default to implementations that always work offline:
 * Ready Player Me for the avatar, and a no-op health source. Swapping in a Samsung Health source or
 * an AI avatar provider is a one-line change here.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class IntegrationsModule {

    @Binds
    @Singleton
    abstract fun bindAvatarProvider(impl: ReadyPlayerMeProvider): AvatarProvider

    @Binds
    @Singleton
    abstract fun bindHealthDataSource(impl: NoopHealthDataSource): HealthDataSource
}
