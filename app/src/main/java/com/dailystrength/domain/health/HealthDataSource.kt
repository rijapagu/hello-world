package com.dailystrength.domain.health

import com.dailystrength.domain.model.SportContext

/**
 * Source of activity data that influences workout generation (steps, and tennis/padel sessions).
 *
 * The default implementation is a no-op so the app is fully functional on any device. The real
 * Samsung Health integration is a drop-in implementation of this interface — it lives behind this
 * seam because the Samsung Health SDK ships as a proprietary AAR that requires partner approval and
 * is not available on public Maven (so it must not be a hard dependency). See ARCHITECTURE.md §9.
 */
interface HealthDataSource {

    /** True when a real health backend is connected and permissions are granted. */
    fun isAvailable(): Boolean

    /** Step count for the given day, or null when unavailable. */
    suspend fun dailySteps(epochDay: Long): Int?

    /**
     * Detects whether the user logged a tennis or padel session on [epochDay], used to pre-fill the
     * "did you already play?" answer and adapt the plan. Returns null when nothing is detected.
     */
    suspend fun detectSport(epochDay: Long): SportContext?
}
