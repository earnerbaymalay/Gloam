package com.gloam.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gloam.data.model.MoodRecord
import com.gloam.ui.components.MoodIndicator
import com.gloam.ui.components.YearInPixels
import com.gloam.ui.theme.MoodColors
import java.time.LocalDate

@Composable
fun CalendarScreen(
    selectedYear: Int,
    moodRecords: List<MoodRecord>,
    onYearChange: (Int) -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentYear = LocalDate.now().year
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onYearChange(selectedYear - 1) }
            ) {
                Icon(Icons.Default.ChevronLeft, "Previous year")
            }
            
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            IconButton(
                onClick = { onYearChange(selectedYear + 1) },
                enabled = selectedYear < currentYear
            ) {
                Icon(Icons.Default.ChevronRight, "Next year")
            }
        }
        
        // Stats summary
        MoodStatsSummary(moodRecords = moodRecords)
        
        // Legend
        MoodLegend()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Year in pixels grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            YearInPixels(
                year = selectedYear,
                moodRecords = moodRecords,
                onDateClick = onDateClick,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun MoodStatsSummary(moodRecords: List<MoodRecord>) {
    val totalDays = moodRecords.size
    val avgMood = if (totalDays > 0) moodRecords.map { it.averageMood }.average() else 0.0
    
    val moodDistribution = (1..5).associateWith { score ->
        moodRecords.count { it.averageMood.toInt() == score }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalDays.toString(),
                    label = "Days logged"
                )
                StatItem(
                    value = if (avgMood > 0) String.format("%.1f", avgMood) else "-",
                    label = "Avg mood"
                )
                StatItem(
                    value = "${((totalDays.toFloat() / 365) * 100).toInt()}%",
                    label = "Consistency"
                )
            }
            
            // Mini distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..5).forEach { score ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = MoodColors.emoji[score] ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${moodDistribution[score] ?: 0}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MoodLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        (1..5).forEach { score ->
            MoodIndicator(
                mood = score.toFloat(),
                size = 16,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
