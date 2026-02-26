package com.timetracker.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val contentColor = if (isLightColor(bgColor)) Color.Black else Color.White

    Card(
        modifier = modifier
            .size(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = bgColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRunning) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (activity.icon.isNotEmpty()) {
                Text(
                    text = activity.icon,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Text(
                text = activity.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (isRunning && elapsedText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = elapsedText,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f),
                )
            }
        }
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299f * color.red + 0.587f * color.green + 0.114f * color.blue
    return luminance > 0.6f
}
