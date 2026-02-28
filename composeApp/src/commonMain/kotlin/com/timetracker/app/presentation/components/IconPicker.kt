package com.timetracker.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EmojiCategory(
    val name: String,
    val emojis: List<String>,
)

val EMOJI_CATEGORIES = listOf(
    EmojiCategory("Work & Study", listOf(
        "\uD83D\uDCBC", "\uD83D\uDCBB", "\uD83D\uDCDA", "\uD83D\uDCD6", "\uD83D\uDCDD",
        "\u270F\uFE0F", "\uD83D\uDCCA", "\uD83D\uDCC8", "\uD83D\uDCC5", "\uD83D\uDCCB",
        "\uD83C\uDF93", "\uD83D\uDD2C", "\uD83D\uDD2D", "\u2699\uFE0F", "\uD83D\uDDA5\uFE0F",
    )),
    EmojiCategory("Health & Fitness", listOf(
        "\uD83C\uDFCB\uFE0F", "\uD83C\uDFC3", "\uD83D\uDEB4", "\uD83E\uDDD8", "\uD83C\uDFCA",
        "\u26BD", "\uD83C\uDFC0", "\uD83C\uDFBE", "\uD83E\uDD4A", "\uD83E\uDDBE",
        "\uD83D\uDC9A", "\uD83C\uDF4E", "\uD83E\uDD57", "\uD83D\uDCA7", "\uD83D\uDCA4",
    )),
    EmojiCategory("Transport & Travel", listOf(
        "\uD83D\uDE97", "\uD83D\uDE8C", "\uD83D\uDE82", "\u2708\uFE0F", "\uD83D\uDEB2",
        "\uD83D\uDEB6", "\uD83D\uDE95", "\uD83C\uDFD6\uFE0F", "\uD83C\uDFD4\uFE0F", "\uD83C\uDF0D",
        "\uD83D\uDDFA\uFE0F", "\uD83C\uDFE0", "\uD83C\uDFE2", "\uD83C\uDFEB", "\uD83C\uDFE5",
    )),
    EmojiCategory("Food & Drink", listOf(
        "\uD83C\uDF73", "\uD83C\uDF54", "\uD83C\uDF55", "\uD83C\uDF5C", "\uD83C\uDF5E",
        "\u2615", "\uD83C\uDF7A", "\uD83E\uDD64", "\uD83C\uDF5D", "\uD83C\uDF71",
        "\uD83C\uDF7D\uFE0F", "\uD83C\uDF70", "\uD83E\uDD52", "\uD83C\uDF4C", "\uD83C\uDF53",
    )),
    EmojiCategory("Entertainment", listOf(
        "\uD83C\uDFAC", "\uD83C\uDFAE", "\uD83C\uDFB5", "\uD83C\uDFB8", "\uD83C\uDFA8",
        "\uD83D\uDCF1", "\uD83D\uDCFA", "\uD83C\uDFAD", "\uD83C\uDFB2", "\uD83C\uDFB3",
        "\uD83D\uDCF7", "\uD83C\uDFA4", "\uD83C\uDFAF", "\uD83E\uDDE9", "\uD83C\uDFAA",
    )),
    EmojiCategory("People & Social", listOf(
        "\uD83D\uDC65", "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67", "\uD83E\uDD1D", "\uD83D\uDCAC", "\uD83D\uDCDE",
        "\u2764\uFE0F", "\uD83C\uDF89", "\uD83C\uDF81", "\uD83D\uDC8D", "\uD83D\uDE0A",
        "\uD83D\uDC76", "\uD83D\uDC36", "\uD83D\uDC31", "\uD83C\uDF39", "\uD83C\uDF1F",
    )),
    EmojiCategory("Home & Chores", listOf(
        "\uD83C\uDFE0", "\uD83D\uDEC1", "\uD83E\uDDF9", "\uD83E\uDDFA", "\uD83E\uDDFD",
        "\uD83D\uDEBF", "\uD83D\uDECB\uFE0F", "\uD83D\uDECC", "\uD83D\uDEAA", "\uD83D\uDD11",
        "\uD83D\uDCA1", "\uD83D\uDEE0\uFE0F", "\uD83E\uDDF0", "\uD83C\uDF31", "\uD83C\uDF3A",
    )),
    EmojiCategory("Shopping & Finance", listOf(
        "\uD83D\uDED2", "\uD83D\uDCB0", "\uD83D\uDCB3", "\uD83D\uDCE6", "\uD83C\uDFE6",
        "\uD83D\uDCB8", "\uD83E\uDDFE", "\uD83C\uDFF7\uFE0F", "\uD83D\uDECD\uFE0F", "\uD83C\uDFEA",
    )),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPicker(
    selectedIcon: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    // Display current icon with tap to open picker
    OutlinedCard(
        onClick = { showDialog = true },
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedIcon.isNotEmpty()) {
                Text(selectedIcon, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = if (selectedIcon.isEmpty()) "Tap to choose icon" else "Tap to change icon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Icon") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(EMOJI_CATEGORIES) { category ->
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        // Use a flow layout style - rows of emojis
                        val rows = category.emojis.chunked(5)
                        rows.forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                row.forEach { emoji ->
                                    val isSelected = emoji == selectedIcon
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = MaterialTheme.shapes.small,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        tonalElevation = if (isSelected) 3.dp else 0.dp,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable {
                                                    onIconSelected(emoji)
                                                    showDialog = false
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(emoji, fontSize = 24.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (selectedIcon.isNotEmpty()) {
                    TextButton(onClick = { onIconSelected(""); showDialog = false }) {
                        Text("Clear")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        )
    }
}
