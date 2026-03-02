package com.gloam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gloam.data.model.JournalEntry
import com.gloam.ui.components.PinLockScreen
import com.gloam.ui.screens.calendar.CalendarScreen
import com.gloam.ui.screens.entries.EntriesScreen
import com.gloam.ui.screens.entries.EntryDetailScreen
import com.gloam.ui.screens.home.HomeScreen
import com.gloam.ui.screens.settings.SettingsScreen
import com.gloam.ui.theme.GloamTheme
import com.gloam.util.NotificationScheduler
import com.gloam.viewmodel.GloamViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

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
                            onBiometricClick = null
                        )
                    } else {
                        GloamApp(
                            viewModel = viewModel,
                            onExportData = { uri -> exportData(uri) }
                        )
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> fetchLocation()
            else -> locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.updateLocation(it.latitude, it.longitude)
                    getSharedPreferences("gloam_prefs", MODE_PRIVATE).edit()
                        .putFloat("latitude", it.latitude.toFloat())
                        .putFloat("longitude", it.longitude.toFloat())
                        .apply()
                    NotificationScheduler.scheduleFromPrefs(this)
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted — solar times fall back to defaults
        }
    }

    // ── PIN — hash stored, never plaintext ────────────────────────────────────

    private fun isPinEnabled(): Boolean =
        getSharedPreferences("gloam_secure", MODE_PRIVATE)
            .getString("pin_hash", null) != null

    private fun verifyPin(pin: String): Boolean {
        val saved = getSharedPreferences("gloam_secure", MODE_PRIVATE)
            .getString("pin_hash", null)
        return saved == hashPin(pin)
    }

    // ── JSON export ───────────────────────────────────────────────────────────

    private fun exportData(uri: Uri) {
        lifecycleScope.launch {
            runCatching {
                val entries = mutableListOf<JournalEntry>()
                val job = launch {
                    viewModel.allEntries.collect { entries.addAll(it); throw Exception("done") }
                }
                job.join()
                writeExportJson(uri, entries)
            }.onFailure { e ->
                if (e.message != "done") {
                    // Unexpected error — silently ignore for now
                }
            }
        }
    }

    private fun writeExportJson(uri: Uri, entries: List<JournalEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date.toString())
                put("type", entry.entryType.name)
                put("mood", entry.moodScore)
                put("response1", entry.prompt1Response)
                put("response2", entry.prompt2Response)
                put("response3", entry.prompt3Response)
                put("created", entry.createdAt.toString())
                put("updated", entry.updatedAt.toString())
            })
        }
        val root = JSONObject().apply {
            put("app", "Gloam")
            put("exported", Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString())
            put("count", entries.size)
            put("entries", array)
        }
        contentResolver.openOutputStream(uri)?.use { it.write(root.toString(2).toByteArray()) }
    }
}

/** SHA-256 of the PIN digits — used both in Activity and GloamApp composable. */
fun hashPin(pin: String): String =
    MessageDigest.getInstance("SHA-256")
        .digest(pin.toByteArray())
        .joinToString("") { "%02x".format(it) }

// ─────────────────────────────────────────────────────────────────────────────
// Navigation graph
// ─────────────────────────────────────────────────────────────────────────────

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : Screen("home", "Today", Icons.Outlined.Home, Icons.Filled.Home)
    object Calendar : Screen("calendar", "Year", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    object Entries : Screen("entries", "Entries", Icons.Outlined.MenuBook, Icons.Filled.MenuBook)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

val screens = listOf(Screen.Home, Screen.Calendar, Screen.Entries, Screen.Settings)

@Composable
fun GloamApp(
    viewModel: GloamViewModel,
    onExportData: (Uri) -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val sunTimes by viewModel.sunTimes.collectAsStateWithLifecycle()
    val currentEntryType by viewModel.currentEntryType.collectAsStateWithLifecycle()
    val todayEntries by viewModel.todayEntries.collectAsStateWithLifecycle()
    val prompts by viewModel.currentPrompts.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val yearMoodRecords by viewModel.yearMoodRecords.collectAsStateWithLifecycle(emptyList())
    val allEntries by viewModel.allEntries.collectAsStateWithLifecycle(emptyList())
    val selectedDateEntries by viewModel.selectedDateEntries.collectAsStateWithLifecycle(emptyList())
    val editingEntry by viewModel.editingEntry.collectAsStateWithLifecycle()

    var showPinSetup by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var filterDate by remember { mutableStateOf<LocalDate?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { onExportData(it) } }

    // PIN setup takes over the whole screen
    if (showPinSetup) {
        PinLockScreen(
            isSetup = true,
            onPinEntered = { pin ->
                context.getSharedPreferences("gloam_secure", Context.MODE_PRIVATE)
                    .edit().putString("pin_hash", hashPin(pin)).apply()
                showPinSetup = false
                true
            }
        )
        return
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Gloam") },
            text = {
                Text(
                    "Version 1.0.0\n\n" +
                    "A minimal journaling app with solar-timed prompts, " +
                    "mood tracking, and privacy-first design.\n\n" +
                    "All data is stored locally in an encrypted database."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Close") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            // Hide nav bar while viewing an entry detail
            if (editingEntry == null) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    screens.forEach { screen ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == screen.route } == true
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
                                viewModel.setEditingEntry(null)
                                if (screen == Screen.Entries) filterDate = null
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
                        filterDate = date
                        viewModel.selectDate(date)
                        navController.navigate(Screen.Entries.route)
                    }
                )
            }

            composable(Screen.Entries.route) {
                // EntryDetailScreen overlays the list when an entry is selected
                if (editingEntry != null) {
                    EntryDetailScreen(
                        entry = editingEntry!!,
                        onBack = { viewModel.setEditingEntry(null) },
                        onSave = { updated ->
                            viewModel.updateEntry(updated)
                            viewModel.setEditingEntry(null)
                        },
                        onDelete = { entry ->
                            viewModel.deleteEntry(entry)
                            viewModel.setEditingEntry(null)
                        }
                    )
                } else {
                    EntriesScreen(
                        entries = if (filterDate != null) selectedDateEntries else allEntries,
                        onEntryClick = { viewModel.setEditingEntry(it) }
                    )
                }
            }

            composable(Screen.Settings.route) {
                val prefs = context.getSharedPreferences("gloam_secure", Context.MODE_PRIVATE)
                val isPinEnabled = prefs.getString("pin_hash", null) != null
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                SettingsScreen(
                    isPinEnabled = isPinEnabled,
                    onPinToggle = { enable ->
                        if (enable) {
                            showPinSetup = true
                        } else {
                            prefs.edit().remove("pin_hash").apply()
                        }
                    },
                    onExportData = {
                        exportLauncher.launch("gloam_export_$today.json")
                    },
                    onAbout = { showAbout = true }
                )
            }
        }
    }
}
