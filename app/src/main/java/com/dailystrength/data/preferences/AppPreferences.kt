package com.dailystrength.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** App-level preferences and feature flags backed by Preferences DataStore. */
@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val aiEnabled: Flow<Boolean> = dataStore.data.map { it[KEY_AI_ENABLED] ?: true }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[KEY_ONBOARDING] ?: false }

    suspend fun isAiEnabled(): Boolean = aiEnabled.first()

    suspend fun setAiEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AI_ENABLED] = enabled }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING] = complete }
    }

    private companion object {
        val KEY_AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
    }
}
