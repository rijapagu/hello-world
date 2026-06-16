package com.dailystrength.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.model.Exercise
import com.dailystrength.domain.model.ExerciseCategory
import com.dailystrength.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LibraryUiState(
    val query: String = "",
    val selectedCategory: ExerciseCategory? = null,
    val exercises: List<Exercise> = emptyList(),
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<ExerciseCategory?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        exerciseRepository.observeAll(),
        query,
        selectedCategory,
    ) { allExercises, currentQuery, category ->
        val trimmed = currentQuery.trim()
        val filtered = allExercises.filter { exercise ->
            val matchesQuery = trimmed.isEmpty() ||
                exercise.name.contains(trimmed, ignoreCase = true)
            val matchesCategory = category == null || exercise.category == category
            matchesQuery && matchesCategory
        }
        LibraryUiState(
            query = currentQuery,
            selectedCategory = category,
            exercises = filtered,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onQuery(value: String) {
        query.value = value
    }

    fun onCategory(category: ExerciseCategory?) {
        selectedCategory.value = category
    }
}
