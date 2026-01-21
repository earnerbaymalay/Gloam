package com.gloam.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gloam.data.model.EntryType
import com.gloam.data.model.JournalEntry
import com.gloam.data.model.Prompt
import com.gloam.ui.components.MoodSelector
import com.gloam.ui.theme.GloamTheme
import com.gloam.util.SunCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    sunTimes: SunCalculator.SunTimes,
    currentEntryType: EntryType,
    todayEntries: List<JournalEntry>,
    prompts: Triple<Prompt, Prompt, Prompt>?,
    onLoadPrompts: (EntryType) -> Unit,
    onSaveEntry: (EntryType, Int, String, String, String, JournalEntry?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Determine which card to show
    val sunriseEntry = todayEntries.find { it.entryType == EntryType.SUNRISE }
    val sunsetEntry = todayEntries.find { it.entryType == EntryType.SUNSET }
    
    LaunchedEffect(currentEntryType, sunriseEntry, sunsetEntry) {
        val needsPrompts = when (currentEntryType) {
            EntryType.SUNRISE -> sunriseEntry == null
            EntryType.SUNSET -> sunsetEntry == null
        }
        if (needsPrompts && prompts == null) {
            onLoadPrompts(currentEntryType)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with sun times
        SunTimesHeader(
            sunTimes = sunTimes,
            currentEntryType = currentEntryType
        )
        
        // Today's date
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Entry cards
        if (currentEntryType == EntryType.SUNRISE) {
            if (sunriseEntry != null) {
                CompletedEntryCard(
                    entry = sunriseEntry,
                    entryType = EntryType.SUNRISE
                )
            } else {
                prompts?.let { (p1, p2, p3) ->
                    JournalEntryCard(
                        entryType = EntryType.SUNRISE,
                        prompt1 = p1,
                        prompt2 = p2,
                        prompt3 = p3,
                        onSave = { mood, r1, r2, r3 ->
                            onSaveEntry(EntryType.SUNRISE, mood, r1, r2, r3, null)
                        }
                    )
                }
            }
        } else {
            // Show sunrise summary if completed
            sunriseEntry?.let {
                CompletedEntryCard(
                    entry = it,
                    entryType = EntryType.SUNRISE,
                    isCompact = true
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (sunsetEntry != null) {
                CompletedEntryCard(
                    entry = sunsetEntry,
                    entryType = EntryType.SUNSET
                )
            } else {
                prompts?.let { (p1, p2, p3) ->
                    JournalEntryCard(
                        entryType = EntryType.SUNSET,
                        prompt1 = p1,
                        prompt2 = p2,
                        prompt3 = p3,
                        onSave = { mood, r1, r2, r3 ->
                            onSaveEntry(EntryType.SUNSET, mood, r1, r2, r3, null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SunTimesHeader(
    sunTimes: SunCalculator.SunTimes,
    currentEntryType: EntryType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sunrise
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.WbSunny,
                contentDescription = "Sunrise",
                tint = if (currentEntryType == EntryType.SUNRISE) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sunTimes.sunrise.format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.bodyMedium,
                color = if (currentEntryType == EntryType.SUNRISE) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sunrise",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Current time indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Now",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Sunset
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.NightsStay,
                contentDescription = "Sunset",
                tint = if (currentEntryType == EntryType.SUNSET) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sunTimes.sunset.format(DateTimeFormatter.ofPattern("h:mm a")),
                style = MaterialTheme.typography.bodyMedium,
                color = if (currentEntryType == EntryType.SUNSET) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sunset",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    entryType: EntryType,
    prompt1: Prompt,
    prompt2: Prompt,
    prompt3: Prompt,
    onSave: (Int, String, String, String) -> Unit
) {
    var selectedMood by remember { mutableStateOf<Int?>(null) }
    var response1 by remember { mutableStateOf("") }
    var response2 by remember { mutableStateOf("") }
    var response3 by remember { mutableStateOf("") }
    
    val canSave = selectedMood != null && response1.isNotBlank()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (entryType == EntryType.SUNRISE) 
                        Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (entryType == EntryType.SUNRISE) "Morning Reflection" else "Evening Reflection",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Mood selector
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MoodSelector(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it }
            )
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            // Prompt 1
            PromptField(
                prompt = prompt1.text,
                value = response1,
                onValueChange = { response1 = it }
            )
            
            // Prompt 2
            PromptField(
                prompt = prompt2.text,
                value = response2,
                onValueChange = { response2 = it }
            )
            
            // Prompt 3
            PromptField(
                prompt = prompt3.text,
                value = response3,
                onValueChange = { response3 = it }
            )
            
            // Save button
            Button(
                onClick = { 
                    selectedMood?.let { mood ->
                        onSave(mood, response1, response2, response3)
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Entry")
            }
        }
    }
}

@Composable
private fun PromptField(
    prompt: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            placeholder = {
                Text(
                    text = "Write your thoughts...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        )
    }
}

@Composable
private fun CompletedEntryCard(
    entry: JournalEntry,
    entryType: EntryType,
    isCompact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (entryType == EntryType.SUNRISE) 
                        Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (entryType == EntryType.SUNRISE) "Morning" else "Evening",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                com.gloam.ui.components.MoodIndicator(
                    mood = entry.moodScore.toFloat(),
                    size = 28
                )
            }
            
            if (!isCompact) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                
                if (entry.prompt1Response.isNotBlank()) {
                    Text(
                        text = entry.prompt1Response,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }
        }
    }
}
