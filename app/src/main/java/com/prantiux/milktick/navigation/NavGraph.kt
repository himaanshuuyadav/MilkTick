package com.prantiux.milktick.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.prantiux.milktick.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Auth.route
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Records.route) {
            RecordsScreen(navController = navController)
        }
        composable(Screen.Summary.route) {
            SummaryScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(navController = navController)
        }
        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(navController = navController)
        }
        composable(Screen.Theme.route) {
            ThemeScreen(navController = navController)
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }
        composable(route = Screen.Calendar.route, arguments = listOf(navArgument("month") { type = NavType.IntType }, navArgument("year") { type = NavType.IntType })) { backStackEntry ->
            val month = backStackEntry.arguments?.getInt("month") ?: 0
            val year = backStackEntry.arguments?.getInt("year") ?: 0
            CalendarScreen(month = month, year = year, navController = navController)
        }
    }
}
