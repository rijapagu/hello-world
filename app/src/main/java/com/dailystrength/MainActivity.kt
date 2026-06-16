package com.dailystrength

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dailystrength.presentation.dashboard.DashboardScreen
import com.dailystrength.presentation.theme.DailyStrengthTheme
import com.dailystrength.work.WidgetRefreshWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. The widget's Quick Start deep link (dailystrength://start) lands here; the
 * dashboard reads the intent to auto-open the start flow.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WidgetRefreshWorker.schedule(applicationContext)

        val startWorkout = intent?.data?.host == "start"

        setContent {
            DailyStrengthTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(autoStart = startWorkout)
                }
            }
        }
    }
}
