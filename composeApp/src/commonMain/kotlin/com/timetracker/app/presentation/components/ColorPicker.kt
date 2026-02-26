package com.timetracker.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PRESET_COLORS = listOf(
    0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
    0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
    0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
    0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722,
    0xFF795548, 0xFF9E9E9E, 0xFF607D8B, 0xFF000000,
).map { it.toInt() }

@Composable
fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier.height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(PRESET_COLORS) { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(color))
                    .then(
                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                            .border(4.dp, Color.Black, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorSelected(color) },
            )
        }
    }
}
