package com.dailystrength.data.avatar

import com.dailystrength.domain.avatar.AvatarProvider
import com.dailystrength.domain.model.AvatarStage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ready Player Me integration. Uses RPM's 2D render endpoint to produce a posed image of the user's
 * avatar without bundling a 3D engine — fully offline-capable for the rest of the app and buildable
 * today. The interactive 3D render (rotate/zoom via SceneView loading the `.glb`) is the documented
 * Phase-2 swap behind [AvatarProvider]. See ARCHITECTURE.md §10.
 *
 * Avatars are glb models at `https://models.readyplayer.me/{id}.glb`; the same id renders to a PNG at
 * `https://models.readyplayer.me/{id}.png`.
 */
@Singleton
class ReadyPlayerMeProvider @Inject constructor() : AvatarProvider {

    override fun renderUrl(avatarId: String?, stage: AvatarStage): String? {
        val id = avatarId?.takeIf { it.isNotBlank() } ?: return null
        // Pose + expression scale with the evolution stage to convey growing athleticism.
        val pose = when (stage) {
            AvatarStage.SEED, AvatarStage.SPARK -> "relaxed"
            AvatarStage.FORGED, AvatarStage.TEMPERED -> "standing"
            else -> "power-stance"
        }
        return "$MODELS_BASE/$id.png?pose=$pose&expression=happy&quality=high&size=512"
    }

    override fun editorUrl(): String = "https://readyplayer.me/avatar"

    override fun parseAvatarId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        // Accept a raw id (24-hex-ish) or any RPM url ending in /{id}.glb or /{id}.png or /{id}.
        val candidate = trimmed
            .substringAfterLast('/')
            .substringBefore('?')
            .removeSuffix(".glb")
            .removeSuffix(".png")
        return candidate.takeIf { it.isNotBlank() && it.all { c -> c.isLetterOrDigit() } }
    }

    private companion object {
        const val MODELS_BASE = "https://models.readyplayer.me"
    }
}
