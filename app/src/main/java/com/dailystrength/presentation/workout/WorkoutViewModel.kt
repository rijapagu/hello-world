package com.dailystrength.presentation.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.repository.ExerciseRepository
import com.dailystrength.domain.repository.WorkoutRepository
import com.dailystrength.domain.usecase.CompleteWorkoutUseCase
import com.dailystrength.presentation.navigation.Routes
import com.dailystrength.widget.WidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A single exercise as presented during execution. */
data class WorkoutItemUi(
    val workoutExerciseId: Long,
    val name: String,
    val instructions: List<String>,
    val targetSets: Int,
    val targetReps: Int?,
    val targetHoldSeconds: Int?,
    val restSeconds: Int,
)

data class WorkoutUiState(
    val loading: Boolean = true,
    val items: List<WorkoutItemUi> = emptyList(),
    val currentIndex: Int = 0,
    val currentSet: Int = 1,
    val repInput: Int = 0,
    val resting: Boolean = false,
    val restLeft: Int = 0,
    val finishing: Boolean = false,
) {
    val current: WorkoutItemUi? get() = items.getOrNull(currentIndex)
    val totalExercises: Int get() = items.size
}

sealed interface WorkoutEvent {
    data class Finished(val currentStreak: Int) : WorkoutEvent
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val completeWorkout: CompleteWorkoutUseCase,
    private val widgetUpdater: WidgetUpdater,
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle[Routes.ARG_WORKOUT_ID])

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _events = Channel<WorkoutEvent>(Channel.BUFFERED)
    val events: Flow<WorkoutEvent> = _events.receiveAsFlow()

    private var restJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val workout = workoutRepository.getWorkout(workoutId)
        if (workout == null) {
            _uiState.update { it.copy(loading = false) }
            return
        }
        val library = exerciseRepository.getAll().associateBy { it.id }
        val items = workout.exercises.map { we ->
            val exercise = library[we.exerciseId]
            WorkoutItemUi(
                workoutExerciseId = we.id,
                name = exercise?.name ?: we.exerciseId,
                instructions = exercise?.instructions ?: emptyList(),
                targetSets = we.targetSets,
                targetReps = we.targetReps,
                targetHoldSeconds = we.targetHoldSeconds,
                restSeconds = we.restSeconds,
            )
        }
        _uiState.update {
            it.copy(
                loading = false,
                items = items,
                repInput = items.firstOrNull()?.targetReps ?: 0,
            )
        }
    }

    fun increment() = _uiState.update { it.copy(repInput = (it.repInput + 1).coerceAtMost(99)) }
    fun decrement() = _uiState.update { it.copy(repInput = (it.repInput - 1).coerceAtLeast(0)) }

    /** Logs the current set and advances the state machine (next set, rest, next exercise, or finish). */
    fun logCurrentSet() {
        val state = _uiState.value
        val item = state.current ?: return
        val reps = if (item.targetHoldSeconds == null) state.repInput else null

        viewModelScope.launch {
            workoutRepository.logSet(
                workoutExerciseId = item.workoutExerciseId,
                setNumber = state.currentSet,
                reps = reps,
                holdSeconds = item.targetHoldSeconds,
            )
            advance(item)
        }
    }

    private fun advance(item: WorkoutItemUi) {
        val state = _uiState.value
        if (state.currentSet < item.targetSets) {
            _uiState.update { it.copy(currentSet = it.currentSet + 1) }
            startRest(item.restSeconds)
        } else {
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.items.size) {
                finish()
            } else {
                val next = state.items[nextIndex]
                _uiState.update {
                    it.copy(currentIndex = nextIndex, currentSet = 1, repInput = next.targetReps ?: 0)
                }
                startRest(item.restSeconds)
            }
        }
    }

    private fun startRest(seconds: Int) {
        restJob?.cancel()
        if (seconds <= 0) return
        _uiState.update { it.copy(resting = true, restLeft = seconds) }
        restJob = viewModelScope.launch {
            var left = seconds
            while (left > 0) {
                delay(1000)
                left--
                _uiState.update { it.copy(restLeft = left) }
            }
            _uiState.update { it.copy(resting = false) }
        }
    }

    fun skipRest() {
        restJob?.cancel()
        _uiState.update { it.copy(resting = false, restLeft = 0) }
    }

    private fun finish() {
        _uiState.update { it.copy(finishing = true) }
        viewModelScope.launch {
            val streak = completeWorkout(workoutId)
            widgetUpdater.updateAll()
            _events.send(WorkoutEvent.Finished(streak.currentStreak))
        }
    }

    override fun onCleared() {
        restJob?.cancel()
        super.onCleared()
    }
}
