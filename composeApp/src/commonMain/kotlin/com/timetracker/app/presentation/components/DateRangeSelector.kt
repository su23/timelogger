package com.timetracker.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class DateRangeOption(val label: String) {
    Day("Day"),
    Week("Week"),
    Month("Month"),
}

@Composable
fun DateRangeSelector(
    selected: DateRangeOption,
    onSelected: (DateRangeOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DateRangeOption.entries.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(option.label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
