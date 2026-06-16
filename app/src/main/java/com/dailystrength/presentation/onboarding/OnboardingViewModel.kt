package com.dailystrength.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.data.auth.GoogleAuthClient
import com.dailystrength.data.preferences.AppPreferences
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val name: String = "",
    val age: String = "30",
    val heightCm: String = "175",
    val weightKg: String = "75",
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val equipment: Set<Equipment> = emptySet(),
    val googleId: String? = null,
    val signedInName: String? = null,
    val saving: Boolean = false,
) {
    val canFinish: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull()?.let { it in 12..100 } == true &&
            heightCm.toIntOrNull()?.let { it in 100..230 } == true &&
            weightKg.toDoubleOrNull()?.let { it in 30.0..250.0 } == true
}

sealed interface OnboardingEvent { data object Done : OnboardingEvent }

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferences: AppPreferences,
    private val googleAuthClient: GoogleAuthClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events: Flow<OnboardingEvent> = _events.receiveAsFlow()

    fun onName(value: String) = _uiState.update { it.copy(name = value) }
    fun onAge(value: String) = _uiState.update { it.copy(age = value.filter(Char::isDigit)) }
    fun onHeight(value: String) = _uiState.update { it.copy(heightCm = value.filter(Char::isDigit)) }
    fun onWeight(value: String) = _uiState.update { it.copy(weightKg = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onLevel(level: FitnessLevel) = _uiState.update { it.copy(fitnessLevel = level) }

    fun toggleEquipment(item: Equipment) = _uiState.update {
        val next = if (item in it.equipment) it.equipment - item else it.equipment + item
        it.copy(equipment = next)
    }

    fun onGoogleSignIn(activityContext: Context) {
        viewModelScope.launch {
            val user = googleAuthClient.signIn(activityContext) ?: return@launch
            _uiState.update {
                it.copy(
                    googleId = user.id,
                    signedInName = user.name,
                    name = if (it.name.isBlank()) user.name.orEmpty() else it.name,
                )
            }
        }
    }

    fun onFinish() {
        val state = _uiState.value
        if (!state.canFinish || state.saving) return
        _uiState.update { it.copy(saving = true) }
        viewModelScope.launch {
            val profile = UserProfile(
                name = state.name.trim(),
                age = state.age.toInt(),
                heightCm = state.heightCm.toInt(),
                weightKg = state.weightKg.toDouble(),
                fitnessLevel = state.fitnessLevel,
                equipment = state.equipment.ifEmpty { setOf(Equipment.NONE) },
                googleId = state.googleId,
            )
            userRepository.saveProfile(profile)
            preferences.setOnboardingComplete(true)
            _events.send(OnboardingEvent.Done)
        }
    }
}
