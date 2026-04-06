# PROJECT_CONTEXT

## 1. Project Overview

### App Name
- MilkTick

### Purpose
- MilkTick is an Android app for tracking daily milk deliveries, monthly rates, monthly cost summaries, payment status, and long-term trends.
- It supports authenticated, user-specific cloud data with reminder notifications and CSV export.

### Summary
- The app is a Kotlin + Jetpack Compose application using Firebase Authentication and Firestore as backend services.
- Users log daily milk entries, set monthly rates/default quantity, view records and analytics, export reports, and manage profile/theme/notification preferences.
- The project is feature-rich and mostly functional, with a few unfinished or legacy areas (noted in section 9).

## 2. Tech Stack

### Language and Build
- Kotlin plugin version: 2.0.21
- Android Gradle Plugin: 8.7.3
- Gradle Wrapper: 9.0-milestone-1
- Java compatibility: 1.8
- Compose plugin: org.jetbrains.kotlin.plugin.compose version 2.0.21

### Android SDK Configuration
- compileSdk: 34
- minSdk: 24
- targetSdk: 34
- applicationId / namespace: com.prantiux.milktick

### Core Libraries and Dependencies
From app/build.gradle.kts:
- androidx.core:core-ktx:1.12.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
- androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0
- androidx.activity:activity-compose:1.8.2
- Compose BOM: androidx.compose:compose-bom:2024.02.00
- Compose UI/material:
  - androidx.compose.ui:ui
  - androidx.compose.ui:ui-graphics
  - androidx.compose.ui:ui-tooling-preview
  - androidx.compose.material3:material3
  - androidx.compose.material:material-icons-extended
- Navigation: androidx.navigation:navigation-compose:2.7.6
- Firebase BOM: com.google.firebase:firebase-bom:32.7.0
  - firebase-auth
  - firebase-firestore
  - firebase-analytics
- WorkManager: androidx.work:work-runtime-ktx:2.9.0
- Date-time: org.jetbrains.kotlinx:kotlinx-datetime:0.5.0
- Image loading: io.coil-kt:coil-compose:2.5.0
- Charts: com.github.PhilJay:MPAndroidChart:v3.1.0

### Build/Config Notes
- Lint is relaxed for release builds:
  - checkReleaseBuilds = false
  - abortOnError = false
- Release signing config and credentials are hardcoded in app/build.gradle.kts (security concern for production repos).

## 3. Architecture

### Pattern Used
- Primary pattern: MVVM
- Layers are clearly separated into:
  - data (models)
  - repository (data access)
  - viewmodel (state and business logic)
  - ui (Compose screens and reusable components)
  - navigation (Compose routes/nav graph)
  - notification (workers + receiver + helper)
  - utils (export/preferences and some legacy notification classes)

### Folder Structure and Package Responsibilities
- app/src/main/java/com/prantiux/milktick/MainActivity.kt
  - App entry, theme binding, onboarding gate, nav host shell, notification setup.

- app/src/main/java/com/prantiux/milktick/data
  - MilkEntry: daily record model
  - MonthlyRate: per-month rate/default quantity model
  - MonthlyPayment: per-month payment model
  - MonthlySummary: summary model container

- app/src/main/java/com/prantiux/milktick/repository
  - AuthRepository: Firebase Auth + user profile writes
  - FirestoreRepository: CRUD for entries/rates/payments and notification settings

- app/src/main/java/com/prantiux/milktick/viewmodel
  - AuthViewModel, AppViewModel, HomeViewModel, RateViewModel, SummaryViewModel, RecordsViewModel, AnalyticsViewModel, CalendarViewModel, DebugViewModel
  - SaveButtonState enum for save button UI lifecycle states

- app/src/main/java/com/prantiux/milktick/ui/screens
  - All composable screens (auth, home, rate, records, summary, settings, profile, onboarding, analytics, calendar, etc.)

- app/src/main/java/com/prantiux/milktick/ui/components
  - Shared UI widgets (bottom nav, app bar, logout dialog, skeleton loading)

- app/src/main/java/com/prantiux/milktick/navigation
  - Screen sealed class route definitions
  - NavGraph composable routing map

- app/src/main/java/com/prantiux/milktick/notification
  - Active notification system (workers, channels, broadcast receiver, scheduling)

- app/src/main/java/com/prantiux/milktick/utils
  - CsvExporter, NotificationPreferences, plus legacy notification helpers/scheduler/receiver not wired in manifest/nav

### Architectural Notes
- Dependency injection is manual (direct repository creation in ViewModels).
- State is managed via MutableStateFlow/StateFlow.
- Coroutines are used for async Firebase operations.

## 4. Features Implemented

### Authentication and User
- Email/password sign in and sign up via Firebase Auth.
- User profile creation/update in Firestore users collection.
- Sign-out flow with app state reset.

### Onboarding
- First-launch onboarding with 3 pages.
- Stores onboarding completion in SharedPreferences (app_preferences).
- Notification permission request integrated into onboarding and auth entry path.

### Daily Milk Tracking
- Home screen supports:
  - brought/not brought toggle
  - quantity and note
  - load/edit today entry
  - save/delete behavior based on edit mode and toggle state
- Default quantity auto-populated from current month rate.

### Monthly Rate Management
- Monthly rate and default quantity setup.
- Month-wise data loading and editing.
- Save button states (loading/saved/hidden) and edit mode UX.

### Records and Calendar
- Year-wise records with month cards.
- Available years fetched from Firestore entries.
- Calendar month detail view with:
  - date-level entry visualization
  - no-delivery tracking
  - per-day detail mapping
  - month export and quick actions from menu

### Summary and Export
- Monthly summary cards (days, liters, cost, quick stats).
- Recent entries list.
- CSV export for monthly data to Documents/MilkTick.
- Year export dialog path exists in records flow.

### Analytics
- Analytics dashboard with MPAndroidChart:
  - monthly comparison chart
  - yearly consumption trend chart
  - cost trend chart
- Derived metrics include average daily consumption and consistency percentage.

### Settings and Preferences
- Settings hub with navigation to:
  - Edit Profile
  - Change Password
  - Notification Settings
  - Rate
  - Theme
  - About
  - Sign out
- Theme customization:
  - AUTO/LIGHT/DARK mode
  - accent color palette
- Notification preferences:
  - master toggle
  - daily/evening/monthly reminders
  - reminder times
  - sync of schedule settings to Firestore

### Notifications
- Active WorkManager-based periodic reminders:
  - daily milk reminder
  - evening reminder
  - monthly rate reminder
- Actionable daily notification (Yes/No).
- Broadcast receiver handles actions and updates/deletes entries.

### Debug Tooling
- Debug screen and DebugViewModel for testing Firebase/auth/data operations and showing test messages.

## 5. Key Files

### App Entry and Navigation
- app/src/main/java/com/prantiux/milktick/MainActivity.kt - app bootstrap, onboarding gate, theme listener, main shell and nav setup.
- app/src/main/java/com/prantiux/milktick/navigation/Screen.kt - sealed route definitions for all navigation destinations.
- app/src/main/java/com/prantiux/milktick/navigation/NavGraph.kt - Compose NavHost route wiring.
- app/src/main/java/com/prantiux/milktick/ui/components/BottomNavigation.kt - animated bottom navigation for main tabs.

### Repositories
- app/src/main/java/com/prantiux/milktick/repository/AuthRepository.kt - Firebase authentication and profile updates.
- app/src/main/java/com/prantiux/milktick/repository/FirestoreRepository.kt - Firestore read/write logic for entries, rates, payments, and notification settings.

### Models
- app/src/main/java/com/prantiux/milktick/data/MilkEntry.kt - daily milk entry schema.
- app/src/main/java/com/prantiux/milktick/data/MonthlyRate.kt - monthly pricing/default quantity schema.
- app/src/main/java/com/prantiux/milktick/data/MonthlyPayment.kt - payment status schema.
- app/src/main/java/com/prantiux/milktick/data/MonthlySummary.kt - monthly aggregate schema.

### ViewModels
- app/src/main/java/com/prantiux/milktick/viewmodel/AuthViewModel.kt - auth state machine and auth actions.
- app/src/main/java/com/prantiux/milktick/viewmodel/AppViewModel.kt - app-global session and refresh trigger.
- app/src/main/java/com/prantiux/milktick/viewmodel/HomeViewModel.kt - today entry lifecycle and save/delete logic.
- app/src/main/java/com/prantiux/milktick/viewmodel/RateViewModel.kt - month rate/default quantity edit-save state.
- app/src/main/java/com/prantiux/milktick/viewmodel/SummaryViewModel.kt - monthly aggregation and CSV export trigger.
- app/src/main/java/com/prantiux/milktick/viewmodel/RecordsViewModel.kt - year-level month summary assembly.
- app/src/main/java/com/prantiux/milktick/viewmodel/CalendarViewModel.kt - calendar month data + payment note/state updates.
- app/src/main/java/com/prantiux/milktick/viewmodel/AnalyticsViewModel.kt - analytics metrics and chart datasets.
- app/src/main/java/com/prantiux/milktick/viewmodel/DebugViewModel.kt - debug test actions and results.

### Notification System
- app/src/main/java/com/prantiux/milktick/notification/NotificationHelper.kt - channels and notification builders/actions.
- app/src/main/java/com/prantiux/milktick/notification/NotificationScheduler.kt - schedules periodic work requests.
- app/src/main/java/com/prantiux/milktick/notification/NotificationReceiver.kt - handles notification action broadcasts.
- app/src/main/java/com/prantiux/milktick/notification/DailyMilkWorker.kt - daily reminder worker.
- app/src/main/java/com/prantiux/milktick/notification/EveningReminderWorker.kt - evening reminder worker with entry check.
- app/src/main/java/com/prantiux/milktick/notification/MonthlyRateWorker.kt - monthly rate reminder worker.

### Screens (Primary)
- app/src/main/java/com/prantiux/milktick/ui/screens/AuthScreen.kt - login/register UI.
- app/src/main/java/com/prantiux/milktick/ui/screens/HomeScreen.kt - daily tracking UI.
- app/src/main/java/com/prantiux/milktick/ui/screens/RateScreen.kt - monthly rate management.
- app/src/main/java/com/prantiux/milktick/ui/screens/RecordsScreen.kt - yearly records and exports.
- app/src/main/java/com/prantiux/milktick/ui/screens/SummaryScreen.kt - monthly summary + CSV export.
- app/src/main/java/com/prantiux/milktick/ui/screens/CalendarScreen.kt - detailed month calendar + payment tracking.
- app/src/main/java/com/prantiux/milktick/ui/screens/AnalyticsScreen.kt - chart-based analytics.
- app/src/main/java/com/prantiux/milktick/ui/screens/SettingsScreen.kt - settings hub.
- app/src/main/java/com/prantiux/milktick/ui/screens/EditProfileScreen.kt - display name update.
- app/src/main/java/com/prantiux/milktick/ui/screens/ChangePasswordScreen.kt - password change UI (logic pending).
- app/src/main/java/com/prantiux/milktick/ui/screens/NotificationSettingsScreen.kt - notification toggle/time settings.
- app/src/main/java/com/prantiux/milktick/ui/screens/ThemeScreen.kt - theme and accent controls.
- app/src/main/java/com/prantiux/milktick/ui/screens/AboutScreen.kt - app info.
- app/src/main/java/com/prantiux/milktick/ui/screens/DebugScreen.kt - debug actions screen.
- app/src/main/java/com/prantiux/milktick/ui/screens/OnboardingScreen.kt - first-run experience.

### Config and Manifest
- build.gradle.kts - top-level plugin versions.
- app/build.gradle.kts - module config/dependencies/signing.
- app/src/main/AndroidManifest.xml - permissions, activity, receiver declarations.
- gradle/wrapper/gradle-wrapper.properties - gradle distribution version.

## 6. Database and Storage

### Cloud Database (Primary)
- Firestore is the primary persistence store.
- Effective hierarchical model used by repository:
  - users/{userId}
  - users/{userId}/entries/{yyyy-MM-dd}
  - users/{userId}/rates/{yyyy-MM}
  - users/{userId}/payments/{yyyy-MM}
  - users/{userId}/notificationSettings/{type}

### Firestore Data Shapes
- entries document fields include:
  - date (string yyyy-MM-dd)
  - quantity (float)
  - brought (boolean)
  - note (nullable string)
  - userId (string)
  - timestamp (long)
  - yearMonth (string yyyy-MM)

- rates document fields include:
  - yearMonth, ratePerLiter, defaultQuantity, userId, timestamp

- payments document fields include:
  - yearMonth, isPaid, paymentNote, paidDate, userId, timestamp

### Local Storage
- SharedPreferences:
  - app_preferences: onboarding_completed
  - theme_preferences: theme mode + accent color
  - notification_prefs: toggles/times/sound/vibration/snooze

### File Storage
- CSV exports written to public Documents/MilkTick via Environment.getExternalStoragePublicDirectory.

### Not Used
- No Room database or local SQL schema exists.
- No DataStore implementation found.

## 7. API and Networking

### Networking Approach
- No custom REST API client layer (no Retrofit/OkHttp/Ktor integrations found).
- Backend communication is via Firebase SDKs:
  - Firebase Authentication
  - Firebase Firestore
  - Firebase Analytics

### API Endpoints and Base URL
- No explicit app-defined base URL or endpoint constants.
- Firestore operations are collection/document API calls in repositories.

### Async and Error Handling
- Firebase Task APIs are bridged with coroutines using await().
- Most repository writes/reads return Result wrappers or nullable models.

## 8. Navigation

### Navigation Type
- Jetpack Navigation Compose (code-based nav graph).
- No XML nav graph file present.

### Declared Routes in Screen
- auth
- home
- rate
- records
- summary
- settings
- edit_profile
- change_password
- notification_settings
- theme
- notifications
- about
- analytics
- calendar/{year}/{month}

### NavGraph Composable Destinations
- All routes above are declared in NavGraph.kt, including argument-based calendar route.

### Main Shell and Bottom Navigation Behavior
- Bottom navigation items shown in component:
  - home
  - records
  - summary
  - settings
- MainActivity determines when bottom nav is visible using mainTabRoutes:
  - home
  - records
  - summary
  - settings
- rate is navigable but intentionally outside bottom-tab route list.

### Key Connections
- AuthScreen routes to Home on successful authentication.
- Records month card routes to Calendar for selected month/year.
- Settings routes to profile/password/notification/theme/about/rate.
- Notification deep-link extras can navigate to home or rate from MainActivity intent handling.

## 9. Current State

### What Is Working Well
- Core app loop (auth -> data entry -> rate -> summary -> records -> analytics) is implemented.
- Firestore-backed CRUD for entries/rates/payments is implemented and used.
- Notification scheduling and actionable notifications are implemented.
- Theme and onboarding preferences persist correctly via SharedPreferences.
- CSV export pipeline exists and is integrated into summary/records/calendar flows.

### Incomplete or Known Gaps
- Change password action is not implemented:
  - ui/screens/ChangePasswordScreen.kt contains TODO and currently only pops back.

- Placeholder/unused screen file:
  - ui/screens/NewSettingsScreen.kt exists but is empty and not routed.

- Legacy/duplicate notification implementation exists under utils package:
  - utils/NotificationHelper.kt
  - utils/NotificationScheduler.kt
  - utils/NotificationActionReceiver.kt
  - Active implementation is in notification package and manifest points to notification.NotificationReceiver.

- notifications route appears under navigation but settings currently sends users to notification_settings (not notifications), so NotificationsScreen is likely unused in main UX.

- DebugViewModel has stale diagnostic strings referencing old delete paths/collection naming assumptions, while FirestoreRepository now uses users/{uid}/entries/{date} hierarchy.

### Technical Risks / Observations
- Manifest requests CAMERA and media/storage permissions that are not evidently used in active feature code paths.
- CSV export uses legacy public external storage API, which can be restrictive on modern scoped-storage behavior.
- Release signing credentials are hardcoded in app/build.gradle.kts (security and compliance risk if repository is shared).
- Lint checks for release are disabled, which can mask production issues.

## 10. What I Want to Achieve

- [Add your goals here]
- [Describe the target features, quality, and timeline]
- [List any refactor priorities or release objectives]
