package com.dailystrength.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.data.preferences.AppPreferences
import com.dailystrength.domain.model.Equipment
import com.dailystrength.domain.model.FitnessLevel
import com.dailystrength.domain.model.UserProfile
import com.dailystrength.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val name: String = "",
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val equipment: Set<Equipment> = emptySet(),
    val googleId: String? = null,
    val aiEnabled: Boolean = true,
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
            age.toIntOrNull()?.let { it in 12..100 } == true &&
            heightCm.toIntOrNull()?.let { it in 100..230 } == true &&
            weightKg.toDoubleOrNull()?.let { it in 30.0..250.0 } == true
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferences: AppPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getProfile()
            val ai = preferences.isAiEnabled()
            _uiState.update {
                if (profile == null) {
                    it.copy(loading = false, aiEnabled = ai)
                } else {
                    it.copy(
                        loading = false,
                        name = profile.name,
                        age = profile.age.toString(),
                        heightCm = profile.heightCm.toString(),
                        weightKg = profile.weightKg.toString(),
                        fitnessLevel = profile.fitnessLevel,
                        equipment = profile.equipment - Equipment.NONE,
                        googleId = profile.googleId,
                        aiEnabled = ai,
                    )
                }
            }
        }
    }

    fun onName(v: String) = _uiState.update { it.copy(name = v, saved = false) }
    fun onAge(v: String) = _uiState.update { it.copy(age = v.filter(Char::isDigit), saved = false) }
    fun onHeight(v: String) = _uiState.update { it.copy(heightCm = v.filter(Char::isDigit), saved = false) }
    fun onWeight(v: String) = _uiState.update { it.copy(weightKg = v.filter { c -> c.isDigit() || c == '.' }, saved = false) }
    fun onLevel(level: FitnessLevel) = _uiState.update { it.copy(fitnessLevel = level, saved = false) }

    fun toggleEquipment(item: Equipment) = _uiState.update {
        val next = if (item in it.equipment) it.equipment - item else it.equipment + item
        it.copy(equipment = next, saved = false)
    }

    fun onToggleAi(enabled: Boolean) {
        _uiState.update { it.copy(aiEnabled = enabled) }
        viewModelScope.launch { preferences.setAiEnabled(enabled) }
    }

    fun onSignOut() {
        _uiState.update { it.copy(googleId = null, saved = false) }
    }

    fun onSave() {
        val s = _uiState.value
        if (!s.canSave) return
        viewModelScope.launch {
            userRepository.saveProfile(
                UserProfile(
                    name = s.name.trim(),
                    age = s.age.toInt(),
                    heightCm = s.heightCm.toInt(),
                    weightKg = s.weightKg.toDouble(),
                    fitnessLevel = s.fitnessLevel,
                    equipment = s.equipment.ifEmpty { setOf(Equipment.NONE) },
                    googleId = s.googleId,
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }
}
