package com.prantiux.milktick.navigation

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Home : Screen("home")
    data object Records : Screen("records")
    data object Settings : Screen("settings")
    data object EditProfile : Screen("edit_profile")
    data object ChangePassword : Screen("change_password")
    data object NotificationSettings : Screen("notification_settings")
    data object Theme : Screen("theme")
    data object Notifications : Screen("notifications")
    data object About : Screen("about")
    data object Analytics : Screen("analytics")
    data object Calendar : Screen("calendar/{year}/{month}?day={day}") {
        fun createRoute(year: Int, month: Int, day: Int? = null): String {
            return if (day != null) {
                "calendar/$year/$month?day=$day"
            } else {
                "calendar/$year/$month"
            }
        }
    }
}
