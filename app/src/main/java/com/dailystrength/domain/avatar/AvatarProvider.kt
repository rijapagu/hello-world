package com.dailystrength.domain.avatar

import com.dailystrength.domain.model.AvatarStage

/**
 * Abstraction over the avatar rendering backend. Phase 1 is Ready Player Me; Phase 2 swaps in an
 * AI-generated provider behind the same interface, so the UI never changes. See ARCHITECTURE.md §10.
 */
interface AvatarProvider {

    /**
     * URL of a rendered image of the user's avatar, posed/styled for the given evolution [stage].
     * Returns null when no avatar is connected yet (the UI then shows a silhouette).
     */
    fun renderUrl(avatarId: String?, stage: AvatarStage): String?

    /** URL of the web avatar creator/editor, where the user creates or updates their avatar. */
    fun editorUrl(): String

    /** Extracts a bare avatar id from a pasted Ready Player Me URL or id, or null if unparseable. */
    fun parseAvatarId(input: String): String?
}
