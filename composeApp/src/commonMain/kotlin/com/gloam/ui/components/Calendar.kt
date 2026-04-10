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
import kotlinx.datetime.*
import kotlin.math.ceil

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private fun daysInMonth(year: Int, month: Int): Int =
    if (month == 2) {
        if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    } else if (month in listOf(4, 6, 9, 11)) 30 else 31

private fun firstDayOfMonth(year: Int, month: Int): Int {
    val date = LocalDate(year, Month(month), 1)
    return date.dayOfWeek.ordinal % 7
}

private fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

@Composable
fun YearInPixels(
    year: Int,
    moodRecords: List<MoodRecord>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val moodMap = moodRecords.associateBy { it.date }
    val today = today()

    Column(modifier = modifier.fillMaxWidth()) {
        // Month headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..12).forEach { month ->
                Text(
                    text = MONTH_NAMES[month - 1].take(1),
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
                        val maxDays = daysInMonth(year, month)
                        val date = if (day <= maxDays) {
                            LocalDate(year, Month(month), day)
                        } else null

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
    val today = today()
    val daysInMonth = daysInMonth(year, month)
    val firstDayOfWeek = firstDayOfMonth(year, month)

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
        val rows = ceil(totalCells / 7.0).toInt()

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
                            val date = LocalDate(year, Month(month), dayOfMonth)
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
