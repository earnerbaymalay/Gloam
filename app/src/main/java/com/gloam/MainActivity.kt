package com.gloam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gloam.ui.components.PinLockScreen
import com.gloam.ui.screens.calendar.CalendarScreen
import com.gloam.ui.screens.entries.EntriesScreen
import com.gloam.ui.screens.home.HomeScreen
import com.gloam.ui.screens.settings.SettingsScreen
import com.gloam.ui.theme.GloamTheme
import com.gloam.util.NotificationScheduler
import com.gloam.viewmodel.GloamViewModel
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    
    private val viewModel: GloamViewModel by viewModels()
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchLocation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestLocationPermission()
        
        setContent {
            val daylightProgress by viewModel.daylightProgress.collectAsStateWithLifecycle()
            val isUnlocked = remember { mutableStateOf(!isPinEnabled()) }
            
            GloamTheme(daylightProgress = daylightProgress) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isPinEnabled() && !isUnlocked.value) {
                        PinLockScreen(
                            onPinEntered = { pin ->
                                val success = verifyPin(pin)
                                if (success) isUnlocked.value = true
                                success
                            },
                            onBiometricClick = null // Can add biometric later
                        )
                    } else {
                        GloamApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
    
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocation()
            }
            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                    
                    // Save and schedule notifications
                    getSharedPreferences("gloam_prefs", MODE_PRIVATE).edit()
                        .putFloat("latitude", it.latitude.toFloat())
                        .putFloat("longitude", it.longitude.toFloat())
                        .apply()
                    
                    NotificationScheduler.scheduleFromPrefs(this)
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
    
    private fun isPinEnabled(): Boolean {
        return getSharedPreferences("gloam_secure", MODE_PRIVATE)
            .getString("pin", null) != null
    }
    
    private fun verifyPin(pin: String): Boolean {
        val savedPin = getSharedPreferences("gloam_secure", MODE_PRIVATE)
            .getString("pin", null)
        return savedPin == pin
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : Screen("home", "Today", Icons.Outlined.Home, Icons.Filled.Home)
    object Calendar : Screen("calendar", "Year", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    object Entries : Screen("entries", "Entries", Icons.Outlined.MenuBook, Icons.Filled.MenuBook)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

val screens = listOf(Screen.Home, Screen.Calendar, Screen.Entries, Screen.Settings)

@Composable
fun GloamApp(viewModel: GloamViewModel) {
    val navController = rememberNavController()
    
    // Collect states
    val sunTimes by viewModel.sunTimes.collectAsStateWithLifecycle()
    val currentEntryType by viewModel.currentEntryType.collectAsStateWithLifecycle()
    val todayEntries by viewModel.todayEntries.collectAsStateWithLifecycle()
    val prompts by viewModel.currentPrompts.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val yearMoodRecords by viewModel.yearMoodRecords.collectAsStateWithLifecycle(emptyList())
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle(emptyList())
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                if (selected) screen.selectedIcon else screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    sunTimes = sunTimes,
                    currentEntryType = currentEntryType,
                    todayEntries = todayEntries,
                    prompts = prompts,
                    onLoadPrompts = { viewModel.loadPromptsForType(it) },
                    onSaveEntry = { type, mood, r1, r2, r3, existing ->
                        viewModel.saveEntry(type, mood, r1, r2, r3, existing)
                    }
                )
            }
            
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    selectedYear = selectedYear,
                    moodRecords = yearMoodRecords,
                    onYearChange = { viewModel.selectYear(it) },
                    onDateClick = { date ->
                        viewModel.selectDate(date)
                        navController.navigate(Screen.Entries.route)
                    }
                )
            }
            
            composable(Screen.Entries.route) {
                EntriesScreen(
                    entries = allEntries,
                    onEntryClick = { entry ->
                        viewModel.setEditingEntry(entry)
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                val context = navController.context
                SettingsScreen(
                    isPinEnabled = context.getSharedPreferences("gloam_secure", android.content.Context.MODE_PRIVATE)
                        .getString("pin", null) != null,
                    onPinToggle = { enabled ->
                        // Navigate to PIN setup or disable
                    },
                    onExportData = {
                        // Export functionality
                    },
                    onAbout = {
                        // Show about dialog
                    }
                )
            }
        }
    }
}
