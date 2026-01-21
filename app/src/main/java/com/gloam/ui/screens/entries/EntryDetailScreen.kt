package com.gloam.ui.screens.entries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gloam.data.model.EntryType
import com.gloam.data.model.JournalEntry
import com.gloam.ui.components.MoodIndicator
import com.gloam.ui.components.MoodSelector
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entry: JournalEntry,
    onBack: () -> Unit,
    onSave: (JournalEntry) -> Unit,
    onDelete: (JournalEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedMood by remember { mutableStateOf(entry.moodScore) }
    var editedResponse1 by remember { mutableStateOf(entry.prompt1Response) }
    var editedResponse2 by remember { mutableStateOf(entry.prompt2Response) }
    var editedResponse3 by remember { mutableStateOf(entry.prompt3Response) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = entry.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete, 
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Entry type header
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (entry.entryType == EntryType.SUNRISE)
                                Icons.Outlined.WbSunny else Icons.Outlined.NightsStay,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = if (entry.entryType == EntryType.SUNRISE) 
                                    "Morning Reflection" else "Evening Reflection",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = entry.createdAt.format(
                                    DateTimeFormatter.ofPattern("h:mm a")
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (!isEditing) {
                        MoodIndicator(mood = entry.moodScore.toFloat(), size = 40)
                    }
                }
            }
            
            // Mood section
            if (isEditing) {
                Text(
                    text = "How were you feeling?",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MoodSelector(
                    selectedMood = editedMood,
                    onMoodSelected = { editedMood = it }
                )
            }
            
            Divider()
            
            // Responses
            ResponseSection(
                label = "Response 1",
                value = if (isEditing) editedResponse1 else entry.prompt1Response,
                isEditing = isEditing,
                onValueChange = { editedResponse1 = it }
            )
            
            if (entry.prompt2Response.isNotBlank() || isEditing) {
                ResponseSection(
                    label = "Response 2",
                    value = if (isEditing) editedResponse2 else entry.prompt2Response,
                    isEditing = isEditing,
                    onValueChange = { editedResponse2 = it }
                )
            }
            
            if (entry.prompt3Response.isNotBlank() || isEditing) {
                ResponseSection(
                    label = "Response 3",
                    value = if (isEditing) editedResponse3 else entry.prompt3Response,
                    isEditing = isEditing,
                    onValueChange = { editedResponse3 = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editedMood = entry.moodScore
                            editedResponse1 = entry.prompt1Response
                            editedResponse2 = entry.prompt2Response
                            editedResponse3 = entry.prompt3Response
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            onSave(
                                entry.copy(
                                    moodScore = editedMood,
                                    prompt1Response = editedResponse1,
                                    prompt2Response = editedResponse2,
                                    prompt3Response = editedResponse3
                                )
                            )
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Edit Entry")
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(entry)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ResponseSection(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Text(
                text = value.ifBlank { "No response" },
                style = MaterialTheme.typography.bodyLarge,
                color = if (value.isBlank()) 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
