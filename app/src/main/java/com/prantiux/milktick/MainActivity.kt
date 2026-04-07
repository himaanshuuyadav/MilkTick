package com.prantiux.milktick

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.prantiux.milktick.R
import com.prantiux.milktick.navigation.NavGraph
import com.prantiux.milktick.navigation.Screen
import com.prantiux.milktick.notification.NotificationHelper
import com.prantiux.milktick.notification.NotificationScheduler
import com.prantiux.milktick.repository.AppGraph
import com.prantiux.milktick.ui.components.BottomNavigation
import com.prantiux.milktick.sync.SyncWorkScheduler
import com.prantiux.milktick.ui.theme.MilkTickTheme
import com.prantiux.milktick.viewmodel.AuthState
import com.prantiux.milktick.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            android.util.Log.d("MainActivity", "Permission ${entry.key} granted: ${entry.value}")
        }
        
        // Schedule notifications after permissions are granted
        if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
            NotificationScheduler.scheduleAllNotifications(this)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AppGraph.initialize(applicationContext)
        SyncWorkScheduler.schedulePeriodicSync(applicationContext, repeatMinutes = 30)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val repo = AppGraph.mainRepository
                if (repo.shouldRunInitialSync(currentUser.uid)) {
                    repo.performInitialSyncIfNeeded(currentUser.uid)
                }
            }
        }
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Request permissions
        requestPermissionsIfNeeded()
        
        // Schedule notifications
        NotificationScheduler.scheduleAllNotifications(this)
        
        setContent {
            val themePreferences = remember { com.prantiux.milktick.ui.theme.ThemePreferences(this) }
            val themeMode = remember { mutableStateOf(themePreferences.getThemeMode()) }
            val accentColor = remember { mutableStateOf(themePreferences.getAccentColor()) }
            
            // Observe changes to theme preferences
            DisposableEffect(Unit) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                    themeMode.value = themePreferences.getThemeMode()
                    accentColor.value = themePreferences.getAccentColor()
                }
                applicationContext.getSharedPreferences("theme_preferences", android.content.Context.MODE_PRIVATE)
                    .registerOnSharedPreferenceChangeListener(listener)
                
                onDispose {
                    applicationContext.getSharedPreferences("theme_preferences", android.content.Context.MODE_PRIVATE)
                        .unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
            
            MilkTickTheme(
                themeMode = themeMode.value,
                accentColor = accentColor.value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MilkTickApp(intent)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
    
    private fun requestPermissionsIfNeeded() {
        // Don't request permissions automatically on startup
        // They will be requested during onboarding or on auth screen
    }
}

@Composable
fun MilkTickApp(intent: Intent? = null) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val view = LocalView.current
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Check if onboarding is completed
    val sharedPreferences = remember { 
        context.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE) 
    }
    val onboardingCompleted = remember { 
        sharedPreferences.getBoolean("onboarding_completed", false) 
    }
    var showOnboarding by remember { mutableStateOf(!onboardingCompleted) }
    
    // Show onboarding if not completed
    if (showOnboarding) {
        com.prantiux.milktick.ui.screens.OnboardingScreen(
            onComplete = { showOnboarding = false }
        )
        return
    }
    
    // Handle navigation from notification
    LaunchedEffect(intent) {
        intent?.getStringExtra("navigate_to")?.let { destination ->
            when (destination) {
                "rates" -> navController.navigate(Screen.Home.route)
                "home" -> navController.navigate(Screen.Home.route)
            }
        }
    }
    
    val startDestination = when (authState) {
        is AuthState.Authenticated -> "home"
        else -> "auth"
    }

    // Define main tab routes
    val mainTabRoutes = listOf(
        Screen.Home.route,
        Screen.Records.route,
        Screen.Summary.route,
        Screen.Settings.route
    )
    
    // Get current route to determine if we should show bottom navigation
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Transparent system bars globally (edge-to-edge)
    LaunchedEffect(currentRoute) {
        val window = (view.context as ComponentActivity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = Color.Transparent.toArgb()
        window.isNavigationBarContrastEnforced = false
    }
    
    val shouldShowBottomNav = currentRoute in mainTabRoutes
    
    if (shouldShowBottomNav) {
        // Main app layout with fixed bottom navigation
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigation(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route -> 
                        // Navigate with single top to avoid multiple instances
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            // Only content slides, navigation stays fixed
            // Don't pass paddingValues here since screens have their own Scaffolds
            NavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
    } else {
        // Full screen for auth and other screens
        NavGraph(
            navController = navController,
            startDestination = startDestination
        )
    }
} 