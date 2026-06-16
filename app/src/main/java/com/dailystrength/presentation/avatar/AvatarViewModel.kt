package com.dailystrength.presentation.avatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailystrength.domain.avatar.AvatarProvider
import com.dailystrength.domain.model.AvatarStage
import com.dailystrength.domain.repository.UserRepository
import com.dailystrength.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MilestoneUi(
    val days: Int,
    val name: String,
    val unlocked: Boolean,
    val isCurrent: Boolean,
)

data class AvatarUiState(
    val loading: Boolean = true,
    val stageName: String = "",
    val progressToNext: Float = 0f,
    val nextMilestoneDays: Int? = null,
    val longestStreak: Int = 0,
    val avatarId: String? = null,
    val renderUrl: String? = null,
    val glbUrl: String? = null,
    val editorUrl: String = "",
    val milestones: List<MilestoneUi> = emptyList(),
)

@HiltViewModel
class AvatarViewModel @Inject constructor(
    observeDashboard: ObserveDashboardUseCase,
    private val userRepository: UserRepository,
    private val avatarProvider: AvatarProvider,
) : ViewModel() {

    val uiState: StateFlow<AvatarUiState> = observeDashboard().map { snapshot ->
        val stage = snapshot.avatarStage
        val basis = maxOf(snapshot.streak.longestStreak, snapshot.streak.currentStreak)
        val avatarId = snapshot.profile?.avatarId
        AvatarUiState(
            loading = false,
            stageName = stage.displayName,
            progressToNext = snapshot.avatarProgressToNext,
            nextMilestoneDays = AvatarStage.entries.firstOrNull { it.milestoneDays > stage.milestoneDays }?.milestoneDays,
            longestStreak = snapshot.streak.longestStreak,
            avatarId = avatarId,
            renderUrl = avatarProvider.renderUrl(avatarId, stage),
            glbUrl = avatarProvider.glbUrl(avatarId),
            editorUrl = avatarProvider.editorUrl(),
            milestones = AvatarStage.entries.map { s ->
                MilestoneUi(
                    days = s.milestoneDays,
                    name = s.displayName,
                    unlocked = basis >= s.milestoneDays,
                    isCurrent = s == stage,
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AvatarUiState())

    /** Connects a Ready Player Me avatar from a pasted id or URL. */
    fun onConnectAvatar(input: String) {
        val id = avatarProvider.parseAvatarId(input) ?: return
        viewModelScope.launch {
            val profile = userRepository.getProfile() ?: return@launch
            userRepository.saveProfile(profile.copy(avatarId = id))
        }
    }
}
