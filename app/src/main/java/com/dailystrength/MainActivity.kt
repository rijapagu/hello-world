package com.dailystrength

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dailystrength.presentation.RootViewModel
import com.dailystrength.presentation.StartDestination
import com.dailystrength.presentation.navigation.DailyStrengthNavHost
import com.dailystrength.presentation.navigation.Routes
import com.dailystrength.presentation.theme.DailyStrengthTheme
import com.dailystrength.work.StreakReminderWorker
import com.dailystrength.work.WidgetRefreshWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host. The widget's Quick Start deep link (dailystrength://start) lands here and
 * auto-opens the start flow on the dashboard.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WidgetRefreshWorker.schedule(applicationContext)
        StreakReminderWorker.schedule(applicationContext)

        val autoStartWorkout = intent?.data?.host == "start"

        setContent {
            DailyStrengthTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: RootViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                    val start by viewModel.startDestination.collectAsStateWithLifecycle()

                    when (start) {
                        StartDestination.Loading -> Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        StartDestination.Onboarding -> DailyStrengthNavHost(Routes.ONBOARDING, autoStartWorkout)
                        StartDestination.Dashboard -> DailyStrengthNavHost(Routes.DASHBOARD, autoStartWorkout)
                    }
                }
            }
        }
    }
}
