package com.calisthenia.app.navigation

sealed class AppDestination(val route: String) {
    data object Onboarding : AppDestination("onboarding")
    data object Dashboard : AppDestination("dashboard")
    data object Workouts : AppDestination("workouts")
    data object Nutrition : AppDestination("nutrition")
    data object Progress : AppDestination("progress")
    data object Settings : AppDestination("settings")
}
