package com.dailystrength.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.usecase.DashboardSnapshot
import com.dailystrength.domain.usecase.GenerateWorkoutUseCase
import com.dailystrength.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val snapshot: DashboardSnapshot? = null,
    val showSportDialog: Boolean = false,
    val generating: Boolean = false,
)

/** One-shot navigation effects. */
sealed interface DashboardEvent {
    data class OpenWorkout(val workoutId: Long) : DashboardEvent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    private val generateWorkout: GenerateWorkoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = Channel<DashboardEvent>(Channel.BUFFERED)
    val events: Flow<DashboardEvent> = _events.receiveAsFlow()

    init {
        observeDashboard()
            .onEach { snapshot -> _uiState.update { it.copy(loading = false, snapshot = snapshot) } }
            .launchIn(viewModelScope)
    }

    fun onStartClicked() {
        _uiState.update { it.copy(showSportDialog = true) }
    }

    fun onDismissSportDialog() {
        _uiState.update { it.copy(showSportDialog = false) }
    }

    /** User answered "¿Tennis/Padel/No?": generate today's adapted workout and open it. */
    fun onSportSelected(sport: SportContext) {
        _uiState.update { it.copy(showSportDialog = false, generating = true) }
        viewModelScope.launch {
            val workout = runCatching { generateWorkout(sportToday = sport, regenerate = true) }.getOrNull()
            _uiState.update { it.copy(generating = false) }
            if (workout != null) _events.send(DashboardEvent.OpenWorkout(workout.id))
        }
    }

    /** Continue an already generated, still-pending workout for today. */
    fun onContinueWorkout() {
        val workout = _uiState.value.snapshot?.todayWorkout ?: return
        viewModelScope.launch { _events.send(DashboardEvent.OpenWorkout(workout.id)) }
    }
}
