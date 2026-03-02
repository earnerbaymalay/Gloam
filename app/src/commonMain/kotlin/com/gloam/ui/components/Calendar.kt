package com.gloam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Short month abbreviation (first letter) for the year-in-pixels header. */
private fun monthLabel(month: Int): String = when (month) {
    1 -> "J"; 2 -> "F"; 3 -> "M"; 4 -> "A"; 5 -> "M"; 6 -> "J"
    7 -> "J"; 8 -> "A"; 9 -> "S"; 10 -> "O"; 11 -> "N"; 12 -> "D"
    else -> ""
}

/** Returns the number of days in a given month/year, accounting for leap years. */
private fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
}

/**
 * Returns the ISO day-of-week for the 1st of the month (1=Monday … 7=Sunday),
 * converted to a 0-based Sunday-first offset (0=Sun, 1=Mon, …, 6=Sat).
 */
private fun firstDayOfWeekOffset(year: Int, month: Int): Int {
    val d = LocalDate(year, month, 1)
    // kotlinx-datetime: dayOfWeek.isoDayNumber: 1=Mon … 7=Sun
    return d.dayOfWeek.isoDayNumber % 7 // Sun→0, Mon→1, …, Sat→6
}

@Composable
fun YearInPixels(
    year: Int,
    moodRecords: List<MoodRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodMap = moodRecords.associateBy { it.date }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Column(modifier = modifier.fillMaxWidth()) {
        // Month headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..12).forEach { month ->
                Text(
                    text = monthLabel(month),
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
                        val date = if (day <= daysInMonth(year, month)) {
                            LocalDate(year, month, day)
                        } else {
                            null
                        }

                        val mood = date?.let { moodMap[it] }
                        val isToday = date == today
                        val isFuture = date?.let { it > today } ?: true

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
    year: Int,
    month: Int,
    moodRecords: List<MoodRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodMap = moodRecords.associateBy { it.date }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysInThisMonth = daysInMonth(year, month)
    val firstDayOffset = firstDayOfWeekOffset(year, month)

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
        val totalCells = firstDayOffset + daysInThisMonth
        val rows = (totalCells + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(rows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - firstDayOffset + 1

                        if (dayOfMonth in 1..daysInThisMonth) {
                            val date = LocalDate(year, month, dayOfMonth)
                            val mood = moodMap[date]
                            val isToday = date == today
                            val isFuture = date > today

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
