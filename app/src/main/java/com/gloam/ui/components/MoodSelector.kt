package com.gloam.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gloam.ui.theme.MoodColors

@Composable
fun MoodSelector(
    selectedMood: Int?,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { score ->
            MoodButton(
                score = score,
                isSelected = selectedMood == score,
                onClick = { onMoodSelected(score) }
            )
        }
    }
}

@Composable
private fun MoodButton(
    score: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        label = "mood_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MoodColors.forScore(score) else Color.Transparent,
        label = "mood_bg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MoodColors.forScore(score) else MoodColors.forScore(score).copy(alpha = 0.5f),
        label = "mood_border"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, CircleShape)
        ) {
            Text(
                text = MoodColors.emoji[score] ?: "😐",
                fontSize = 28.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = MoodColors.labels[score] ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MoodColors.forScore(score) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MoodIndicator(
    mood: Float,
    size: Int = 24,
    modifier: Modifier = Modifier
) {
    val score = mood.toInt().coerceIn(1, 5)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MoodColors.forScore(score)),
        contentAlignment = Alignment.Center
    ) {
        if (size >= 20) {
            Text(
                text = MoodColors.emoji[score] ?: "",
                fontSize = (size / 2).sp
            )
        }
    }
}
