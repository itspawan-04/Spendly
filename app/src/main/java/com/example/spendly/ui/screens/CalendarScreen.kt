package com.example.spendly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.util.formatMinorUnits
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onDaySelected: (Long) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    var ym by remember { mutableStateOf(YearMonth.now()) }
    var totals by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    LaunchedEffect(ym) {
        totals = viewModel.dailyTotals(ym)
    }
    val budget = prefs.monthlyBudgetMinor
    val daysInMonth = ym.lengthOfMonth()
    val avgDaily = if (budget != null && budget > 0) budget.toDouble() / daysInMonth else null
    val maxDay = (totals.values.maxOrNull() ?: 0L).coerceAtLeast(1L)

    val firstDow = ym.atDay(1).dayOfWeek
    val offset = (firstDow.value + 6) % 7
    val monthDays = (1..daysInMonth).map { ym.atDay(it) }
    val cells: List<LocalDate?> = List(offset) { null } + monthDays
    val paddedSize = ((cells.size + 6) / 7) * 7
    val padded = cells + List(paddedSize - cells.size) { null }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { ym = ym.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(
                    ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = { ym = ym.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = "Next month")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(padded.size) { idx ->
                val date = padded[idx]
                if (date == null) {
                    Box(Modifier.aspectRatio(1f))
                } else {
                    val total = totals[date.toEpochDay()] ?: 0L
                    val heat = heatColor(total, maxDay, avgDaily, isDark)
                    val cellTextColor = textColorForHeatCell(heat)
                    Box(
                        Modifier
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.small)
                            .background(heat)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .clickable { onDaySelected(date.toEpochDay()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = cellTextColor,
                            )
                            if (total > 0) {
                                Text(
                                    formatMinorUnits(total, prefs.currencyCode).take(6),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    color = cellTextColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun textColorForHeatCell(background: Color): Color =
    if (background.luminance() > 0.42f) Color(0xFF0F172A) else Color(0xFFF8FAFC)

private fun heatColor(amount: Long, maxDay: Long, avgDaily: Double?, isDark: Boolean): Color {
    if (amount == 0L) {
        return if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }
    val ratio = if (avgDaily != null && avgDaily > 0) {
        (amount / avgDaily).coerceIn(0.0, 2.0)
    } else {
        amount.toDouble() / maxDay.toDouble()
    }
    return if (isDark) {
        when {
            ratio < 0.5 -> Color(0xFF064E3B)
            ratio < 1.0 -> Color(0xFF047857)
            ratio < 1.25 -> Color(0xFF854D0E)
            ratio < 1.5 -> Color(0xFF9A3412)
            else -> Color(0xFF7F1D1D)
        }
    } else {
        when {
            ratio < 0.5 -> Color(0xFFD1FAE5)
            ratio < 1.0 -> Color(0xFFA7F3D0)
            ratio < 1.25 -> Color(0xFFFDE68A)
            ratio < 1.5 -> Color(0xFFFDBA74)
            else -> Color(0xFFFCA5A5)
        }
    }
}
