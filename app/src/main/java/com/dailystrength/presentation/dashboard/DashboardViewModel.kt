package com.dailystrength.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.model.SportContext
import com.dailystrength.domain.model.Workout
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.usecase.CompleteWorkoutUseCase
import com.dailystrength.domain.usecase.DashboardSnapshot
import com.dailystrength.domain.usecase.GenerateWorkoutUseCase
import com.dailystrength.domain.usecase.ObserveDashboardUseCase
import com.dailystrength.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val snapshot: DashboardSnapshot? = null,
    val showSportDialog: Boolean = false,
    val generating: Boolean = false,
    val celebrateStreak: Int? = null, // non-null right after completing, drives the celebration
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    private val generateWorkout: GenerateWorkoutUseCase,
    private val completeWorkout: CompleteWorkoutUseCase,
    private val workoutRepository: WorkoutRepository,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboard()
            .onEach { snapshot ->
                _uiState.update { it.copy(loading = false, snapshot = snapshot) }
            }
            .launchIn(viewModelScope)
    }

    fun onStartClicked() {
        _uiState.update { it.copy(showSportDialog = true) }
    }

    fun onDismissSportDialog() {
        _uiState.update { it.copy(showSportDialog = false) }
    }

    /** User answered "¿Tennis/Padel/No?": generate (or regenerate) today's adapted workout. */
    fun onSportSelected(sport: SportContext) {
        _uiState.update { it.copy(showSportDialog = false, generating = true) }
        viewModelScope.launch {
            runCatching { generateWorkout(sportToday = sport, regenerate = true) }
            _uiState.update { it.copy(generating = false) }
        }
    }

    /**
     * Quick-completes today's workout by logging the prescribed targets for each set, then advancing
     * the streak and refreshing the widget. A full set-by-set logging UI arrives in Phase 1.
     */
    fun onCompleteToday() {
        val workout = _uiState.value.snapshot?.todayWorkout ?: return
        viewModelScope.launch {
            logTargets(workout)
            val streak = completeWorkout(workout.id)
            widgetUpdater.updateAll()
            _uiState.update { it.copy(celebrateStreak = streak.currentStreak) }
        }
    }

    fun onCelebrationShown() {
        _uiState.update { it.copy(celebrateStreak = null) }
    }

    private suspend fun logTargets(workout: Workout) {
        for (exercise in workout.exercises) {
            if (exercise.completedSets.isNotEmpty()) continue
            for (setNumber in 1..exercise.targetSets) {
                workoutRepository.logSet(
                    workoutExerciseId = exercise.id,
                    setNumber = setNumber,
                    reps = exercise.targetReps,
                    holdSeconds = exercise.targetHoldSeconds,
                )
            }
        }
    }
}
