package com.dailystrength.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Forces an immediate refresh of all streak widgets. Called after a workout is completed and by the
 * midnight refresh worker so the streak stays in sync without waiting for the periodic update.
 */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun updateAll() {
        StreakWidget().updateAll(context)
    }
}
