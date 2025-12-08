package com.prantiux.milktick.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Rate : Screen("rate")
    data object Records : Screen("records")
    data object Summary : Screen("summary")
    data object Settings : Screen("settings")
    data object EditProfile : Screen("edit_profile")
    data object ChangePassword : Screen("change_password")
    data object NotificationSettings : Screen("notification_settings")
    data object Theme : Screen("theme")
    data object Notifications : Screen("notifications")
    data object About : Screen("about")
    data object Analytics : Screen("analytics")
    data object Calendar : Screen("calendar/{year}/{month}") {
        fun createRoute(year: Int, month: Int) = "calendar/$year/$month"
    }
}
