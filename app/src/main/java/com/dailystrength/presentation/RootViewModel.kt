package com.dailystrength.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Decides the start destination: onboarding until the user has completed their profile. */
@HiltViewModel
class RootViewModel @Inject constructor(
    preferences: AppPreferences,
) : ViewModel() {

    val startDestination: StateFlow<StartDestination> = preferences.onboardingComplete
        .map { if (it) StartDestination.Dashboard else StartDestination.Onboarding }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StartDestination.Loading)
}

enum class StartDestination { Loading, Onboarding, Dashboard }
