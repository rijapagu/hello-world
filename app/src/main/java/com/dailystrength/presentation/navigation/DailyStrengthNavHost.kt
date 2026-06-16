package com.dailystrength.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dailystrength.presentation.avatar.AvatarScreen
import com.dailystrength.presentation.dashboard.DashboardScreen
import com.dailystrength.presentation.library.ExerciseDetailScreen
import com.dailystrength.presentation.library.LibraryScreen
import com.dailystrength.presentation.onboarding.OnboardingScreen
import com.dailystrength.presentation.stats.StatsScreen
import com.dailystrength.presentation.workout.WorkoutCompleteScreen
import com.dailystrength.presentation.workout.WorkoutScreen

/**
 * Single-activity navigation graph. [startDestination] is decided by onboarding state; [autoStartWorkout]
 * is true when launched from the widget's Quick Start deep link.
 */
@Composable
fun DailyStrengthNavHost(
    startDestination: String,
    autoStartWorkout: Boolean,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                autoStart = autoStartWorkout,
                onOpenWorkout = { workoutId -> navController.navigate(Routes.workout(workoutId)) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                onOpenAvatar = { navController.navigate(Routes.AVATAR) },
            )
        }

        composable(
            route = Routes.WORKOUT,
            arguments = listOf(navArgument(Routes.ARG_WORKOUT_ID) { type = NavType.LongType }),
        ) {
            WorkoutScreen(
                onFinished = { streak ->
                    navController.navigate(Routes.workoutComplete(streak)) {
                        popUpTo(Routes.WORKOUT) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.WORKOUT_COMPLETE,
            arguments = listOf(navArgument(Routes.ARG_STREAK) { type = NavType.IntType }),
        ) { backStackEntry ->
            val streak = backStackEntry.arguments?.getInt(Routes.ARG_STREAK) ?: 0
            WorkoutCompleteScreen(
                streak = streak,
                onDone = { navController.popBackStack(Routes.DASHBOARD, inclusive = false) },
            )
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.LIBRARY) {
            LibraryScreen(
                onOpenExercise = { exerciseId -> navController.navigate(Routes.exerciseDetail(exerciseId)) },
            )
        }

        composable(
            route = Routes.EXERCISE_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_EXERCISE_ID) { type = NavType.StringType }),
        ) {
            ExerciseDetailScreen()
        }

        composable(Routes.AVATAR) {
            AvatarScreen()
        }
    }
}
