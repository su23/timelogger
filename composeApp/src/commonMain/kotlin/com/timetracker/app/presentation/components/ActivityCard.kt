package com.timetracker.app.presentation.components

import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetracker.app.domain.model.Activity

@Composable
fun ActivityCard(
    activity: Activity,
    isRunning: Boolean,
    elapsedText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = Color(activity.color)

    val pulseAlpha = if (isRunning) {
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        )
        alpha
    } else {
        0f
    }

    Card(
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) {
                bgColor.copy(alpha = pulseAlpha)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRunning) 4.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Circular colored icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                if (activity.icon.isNotEmpty()) {
                    Text(
                        text = activity.icon,
                        fontSize = 22.sp,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                } else {
                    Text(
                        text = activity.name.take(1).uppercase(),
                        color = if (isLightColor(bgColor)) Color.Black else Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Activity name
            Text(
                text = activity.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Running timer display
            if (isRunning && elapsedText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = elapsedText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance > 0.6f
}
