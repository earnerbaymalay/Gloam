package com.gloam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloam.data.model.MoodRecord
import com.gloam.ui.theme.MoodColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun YearInPixels(
    year: Int,
    moodRecords: List<MoodRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodMap = moodRecords.associateBy { it.date }
    val today = LocalDate.now()
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Month headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..12).forEach { month ->
                Text(
                    text = YearMonth.of(year, month).month
                        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        .take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grid: 31 rows x 12 columns
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            (1..31).forEach { day ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    (1..12).forEach { month ->
                        val date = try {
                            LocalDate.of(year, month, day)
                        } catch (e: Exception) {
                            null
                        }
                        
                        val mood = date?.let { moodMap[it] }
                        val isToday = date == today
                        val isFuture = date?.isAfter(today) ?: true
                        
                        PixelCell(
                            mood = mood?.averageMood,
                            isToday = isToday,
                            isFuture = isFuture,
                            isValid = date != null,
                            onClick = { date?.let { onDateClick(it) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PixelCell(
    mood: Float?,
    isToday: Boolean,
    isFuture: Boolean,
    isValid: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        !isValid -> Color.Transparent
        isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        mood != null -> MoodColors.forScore(mood.toInt().coerceIn(1, 5))
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .then(
                if (isToday) Modifier.border(
                    1.5.dp,
                    borderColor,
                    RoundedCornerShape(2.dp)
                ) else Modifier
            )
            .clickable(enabled = isValid && !isFuture) { onClick() }
    )
}

@Composable
fun MonthCalendar(
    yearMonth: YearMonth,
    moodRecords: List<MoodRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodMap = moodRecords.associateBy { it.date }
    val today = LocalDate.now()
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    
    Column(modifier = modifier) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7
        
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - firstDayOfWeek + 1
                        
                        if (dayOfMonth in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayOfMonth)
                            val mood = moodMap[date]
                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            
                            DayCell(
                                day = dayOfMonth,
                                mood = mood?.averageMood,
                                isToday = isToday,
                                isFuture = isFuture,
                                onClick = { onDateClick(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    mood: Float?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isFuture -> Color.Transparent
        mood != null -> MoodColors.forScore(mood.toInt().coerceIn(1, 5))
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable(enabled = !isFuture) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = when {
                mood != null -> Color.White
                isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
