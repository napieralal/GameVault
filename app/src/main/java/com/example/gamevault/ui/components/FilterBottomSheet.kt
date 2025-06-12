package com.example.gamevault.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.TextField
import com.example.gamevault.ui.screens.search.FilterType
import com.example.gamevault.ui.screens.search.SearchFilters
import com.example.gamevault.ui.screens.search.SearchViewModel
import androidx.compose.foundation.clickable
import androidx.compose.material3.TextFieldDefaults

@Composable
fun FilterBottomSheetContent(
    viewModel: SearchViewModel,
    pendingFilters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    onApply: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val sections = viewModel.getFilterSections(pendingFilters, onFiltersChange)
    var activeSection by remember { mutableStateOf(sections.firstOrNull()?.title) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Advanced Filters",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        val selectedChips = sections.flatMap { section ->
            section.items
                .filter { section.selectedIds.contains(it.id) }
                .map { item ->
                    Pair(item.label, { section.onToggle?.invoke(item.id) })
                }
        }

        if (selectedChips.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedChips.forEach { (label, onRemove) ->
                    FilterChip(
                        selected = true,
                        onClick = {onRemove()},
                        label = { Text(label, color = MaterialTheme.colorScheme.onPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // MENU SEKCJI (genres, modes, perspective, itd.)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sections.forEach { section ->
                FilterChip(
                    selected = section.title == activeSection,
                    onClick = { activeSection = section.title },
                    label = {
                        Text(
                            section.title,
                            color = if (section.title == activeSection)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentSection = sections.find { it.title == activeSection }
        currentSection?.let { section ->
            Text(
                section.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (section.type) {
                FilterType.CHECKBOX -> {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        section.items.forEach { item ->
                            FilterChip(
                                selected = section.selectedIds.contains(item.id),
                                onClick = { section.onToggle?.invoke(item.id) },
                                label = {
                                    Text(
                                        item.label,
                                        color = if (section.selectedIds.contains(item.id))
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }

                FilterType.RANGE -> {
                    var from by remember { mutableStateOf(section.selectedRange?.start ?: section.range?.start ?: 0) }
                    var to by remember { mutableStateOf(section.selectedRange?.endInclusive ?: section.range?.endInclusive ?: 100) }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = from.toString(),
                            onValueChange = {
                                from = it.toIntOrNull() ?: from
                                section.onRangeChange?.invoke(from..to)
                            },
                            label = { Text("From", color = MaterialTheme.colorScheme.onSurface) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = to.toString(),
                            onValueChange = {
                                to = it.toIntOrNull() ?: to
                                section.onRangeChange?.invoke(from..to)
                            },
                            label = { Text("To", color = MaterialTheme.colorScheme.onSurface) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    RangeSlider(
                        value = from.toFloat()..to.toFloat(),
                        onValueChange = {
                            from = it.start.toInt()
                            to = it.endInclusive.toInt()
                            section.onRangeChange?.invoke(from..to)
                        },
                        valueRange = section.range!!.start.toFloat()..section.range.endInclusive.toFloat(),
                        steps = 10,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
                            thumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Apply",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onApply(pendingFilters) }
                    .padding(8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
