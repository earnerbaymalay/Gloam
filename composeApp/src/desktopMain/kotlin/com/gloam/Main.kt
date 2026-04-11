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

import kotlinx.coroutines.runBlocking

fun main() = application {
    val dbPath = File(System.getProperty("user.home"), ".gloam/gloam.db").apply {
        parentFile.mkdirs()
    }.absolutePath

    val database = remember { DesktopGloamDatabase(dbPath) }
    val repository = remember { GloamRepository(database) }
    val viewModel = remember { GloamViewModel(repository) }

    Window(
        onCloseRequest = {
            runCatching { runBlocking { database.close() } }
            exitApplication()
        },
        title = "Gloam — Solar-Timed Journaling",
        state = WindowState(width = 1100.dp, height = 800.dp),
    ) {
        val uiState by viewModel.uiState.collectAsState()
        
        GloamTheme(daylightProgress = uiState.daylightProgress) {
            if (uiState.sunTimes != null) {
                GloamDesktopApp(viewModel)
            } else {
                // Show loading or fallback
            }
        }
    }
}

@Composable
@Preview
fun GloamDesktopApp(viewModel: GloamViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
    uiState.sunTimes?.let { sunTimes ->
        com.gloam.ui.screens.home.HomeScreen(
            sunTimes = sunTimes,
            currentEntryType = uiState.currentEntryType,
            todayEntries = uiState.todayEntries,
            prompts = uiState.currentPrompts,
            onLoadPrompts = { viewModel.loadPromptsForType(uiState.currentEntryType) },
            onSaveEntry = { type, mood, r1, r2, r3, existing ->
                // Create and save entry
            }
        )
    }
}
