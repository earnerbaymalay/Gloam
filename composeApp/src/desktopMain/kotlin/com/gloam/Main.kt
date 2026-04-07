package com.gloam

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.gloam.data.db.DesktopGloamDatabase
import com.gloam.data.repository.GloamRepository
import com.gloam.ui.theme.GloamTheme
import com.gloam.ui.theme.GloamTypography
import com.gloam.viewmodel.GloamViewModel
import java.io.File

fun main() = application {
    val dbPath = File(System.getProperty("user.home"), ".gloam/gloam.db").apply {
        parentFile.mkdirs()
    }.absolutePath

    val database = remember { DesktopGloamDatabase(dbPath) }
    val repository = remember { GloamRepository(database) }
    val viewModel = remember { GloamViewModel(repository) }

    Window(
        onCloseRequest = {
            runCatching { database.close() }
            exitApplication()
        },
        title = "Gloam — Solar-Timed Journaling",
        state = WindowState(width = 1100.dp, height = 800.dp),
    ) {
        GloamTheme(daylightProgress = viewModel.uiState.collectAsState().value.daylightProgress) {
            GloamDesktopApp(viewModel)
        }
    }
}

@Composable
@Preview
fun GloamDesktopApp(viewModel: GloamViewModel) {
    // Desktop-specific app shell with window chrome handled by OS
    // Reuses all shared Compose UI components from commonMain
    val uiState by viewModel.uiState.collectAsState()

    // Simple single-window layout for desktop
    GloamAppContent(
        uiState = uiState,
        viewModel = viewModel
    )
}

@Composable
private fun GloamAppContent(
    uiState: com.gloam.viewmodel.GloamUiState,
    viewModel: GloamViewModel
) {
    // Placeholder: reuse HomeScreen from commonMain
    // In production, this would be a full desktop layout with sidebar navigation
    com.gloam.ui.screens.home.HomeScreen(
        sunTimes = uiState.sunTimes,
        currentEntryType = uiState.currentEntryType,
        todayEntries = uiState.todayEntries,
        prompts = uiState.currentPrompts?.let { listOf(it.first, it.second, it.third) } ?: emptyList(),
        onLoadPrompts = { viewModel.loadPromptsForType(uiState.currentEntryType) },
        onSaveEntry = { type, mood, r1, r2, r3 ->
            // Create and save entry
        }
    )
}
