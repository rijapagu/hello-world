package com.dailystrength.domain.util

import java.time.LocalDate
import java.time.ZoneId

/**
 * Abstraction over "today" so the streak/workout logic is testable and timezone-correct. Implemented
 * with the system clock in production; faked in tests.
 */
interface DateProvider {
    fun todayEpochDay(): Long
    fun nowEpochMillis(): Long
}

class SystemDateProvider(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : DateProvider {
    override fun todayEpochDay(): Long = LocalDate.now(zoneId).toEpochDay()
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
