package com.dailystrength.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.model.ProgressSeries
import com.dailystrength.domain.repository.ProgressRepository
import com.dailystrength.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** One key lift's progression, ready for display: a sparkline plus first/latest best reps. */
data class LiftProgress(
    val exerciseId: String,
    val label: String,
    val points: List<Float>,
    val firstBestReps: Int?,
    val latestBestReps: Int?,
) {
    /** Reps gained from the first recorded day to the latest, or null when not computable. */
    val delta: Int?
        get() {
            val first = firstBestReps ?: return null
            val latest = latestBestReps ?: return null
            return latest - first
        }
}

data class StatsUiState(
    val loading: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalWorkouts: Int = 0,
    val trainingStage: String = "",
    val lifts: List<LiftProgress> = emptyList(),
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    private val progressRepository: ProgressRepository,
) : ViewModel() {

    private val trackedLifts: List<Pair<String, String>> = listOf(
        "assisted_pullup" to "Dominadas",
        "push_up" to "Flexiones",
        "dip" to "Fondos",
        "ab_wheel_rollout" to "Ab Wheel",
    )

    private val seriesFlows = trackedLifts.map { (id, label) ->
        progressRepository.observeSeries(id, label)
    }

    val uiState: StateFlow<StatsUiState> = combine(
        observeDashboard(),
        combine(seriesFlows) { it.toList() },
    ) { snapshot, seriesList ->
        StatsUiState(
            loading = false,
            currentStreak = snapshot.streak.currentStreak,
            longestStreak = snapshot.streak.longestStreak,
            totalWorkouts = snapshot.streak.totalWorkouts,
            trainingStage = snapshot.avatarStage.displayName,
            lifts = seriesList.map { it.toLiftProgress() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    private fun ProgressSeries.toLiftProgress(): LiftProgress {
        // Strength progression is tracked as best single-set reps for every key lift
        // (including the ab wheel). Hold-only points are dropped.
        val reps = points.mapNotNull { it.bestReps }
        return LiftProgress(
            exerciseId = exerciseId,
            label = label,
            points = reps.map { it.toFloat() },
            firstBestReps = firstBestReps,
            latestBestReps = latestBestReps,
        )
    }
}
