package com.dailystrength.data.health

import com.dailystrength.domain.health.HealthDataSource
import com.dailystrength.domain.model.SportContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default health source for devices without Samsung Health (or before it is wired). It reports no
 * data, so the user simply answers the sport question manually — the app never depends on health
 * integration to function (Never Zero).
 */
@Singleton
class NoopHealthDataSource @Inject constructor() : HealthDataSource {
    override fun isAvailable(): Boolean = false
    override suspend fun dailySteps(epochDay: Long): Int? = null
    override suspend fun detectSport(epochDay: Long): SportContext? = null
}
