package com.calisthenia.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.calisthenia.app.dashboard.DashboardRoute
import com.calisthenia.feature.onboarding.OnboardingRoute
import com.calisthenia.feature.progress.ProgressRoute
import com.calisthenia.feature.settings.SettingsRoute
import com.calisthenia.feature.workouts.WorkoutsRoute
import com.calisthenia.feature.nutrition.NutritionRoute

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Onboarding.route,
    ) {
        composable(AppDestination.Onboarding.route) {
            OnboardingRoute(onFinish = {
                navController.navigate(AppDestination.Dashboard.route) {
                    popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                }
            })
        }
        composable(AppDestination.Dashboard.route) {
            DashboardRoute(
                onNavigateToWorkouts = { navController.navigate(AppDestination.Workouts.route) },
                onNavigateToNutrition = { navController.navigate(AppDestination.Nutrition.route) },
                onNavigateToProgress = { navController.navigate(AppDestination.Progress.route) },
                onNavigateToSettings = { navController.navigate(AppDestination.Settings.route) },
            )
        }
        composable(AppDestination.Workouts.route) {
            WorkoutsRoute(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Nutrition.route) {
            NutritionRoute(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Progress.route) {
            ProgressRoute(onBack = { navController.popBackStack() })
        }
        composable(AppDestination.Settings.route) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
