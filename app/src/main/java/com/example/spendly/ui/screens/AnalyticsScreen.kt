package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.util.formatMinorUnits
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: SpendlyViewModel, prefs: UserPreferences) {
    val ym = remember { YearMonth.now() }
    var trend by remember { mutableStateOf<List<Pair<YearMonth, Long>>>(emptyList()) }
    var breakdown by remember { mutableStateOf<List<Pair<CategoryEntity, Long>>>(emptyList()) }
    var topDays by remember { mutableStateOf<List<Pair<java.time.LocalDate, Long>>>(emptyList()) }
    var dow by remember { mutableStateOf<Map<DayOfWeek, Long>>(emptyMap()) }
    LaunchedEffect(ym) {
        trend = viewModel.trendMonths()
        breakdown = viewModel.categoryBreakdown(ym)
        topDays = viewModel.topDays(ym)
        dow = viewModel.dayOfWeekTotals(ym)
    }
    val maxTrend = (trend.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    val maxBreak = (breakdown.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    val maxDow = (dow.values.maxOrNull() ?: 1L).coerceAtLeast(1L)
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val prog = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Patterns for the current month and recent history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        AnalyticsCard(title = "6-month trend") {
            trend.forEach { (m, total) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        m.format(java.time.format.DateTimeFormatter.ofPattern("MMM yy", Locale.getDefault())),
                        modifier = Modifier.weight(0.35f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { total.toFloat() / maxTrend.toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = prog,
                        trackColor = track,
                    )
                    Text(
                        formatMinorUnits(total, prefs.currencyCode),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        AnalyticsCard(title = "This month by category") {
            breakdown.forEach { (cat, total) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(cat.iconEmoji, style = MaterialTheme.typography.titleMedium)
                    Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { total.toFloat() / maxBreak.toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = prog,
                        trackColor = track,
                    )
                    Text(formatMinorUnits(total, prefs.currencyCode), style = MaterialTheme.typography.labelLarge)
                }
            }
            if (breakdown.isEmpty()) {
                Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AnalyticsCard(title = "Top spending days") {
            topDays.forEach { (day, total) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day.toString(), style = MaterialTheme.typography.bodyMedium)
                    Text(formatMinorUnits(total, prefs.currencyCode), style = MaterialTheme.typography.titleSmall)
                }
            }
            if (topDays.isEmpty()) {
                Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AnalyticsCard(title = "Spend by weekday") {
            DayOfWeek.entries.forEach { day ->
                val total = dow[day] ?: 0L
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        modifier = Modifier.weight(0.3f),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    LinearProgressIndicator(
                        progress = { total.toFloat() / maxDow.toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = prog,
                        trackColor = track,
                    )
                    Text(formatMinorUnits(total, prefs.currencyCode), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(PaddingValues(horizontal = 18.dp, vertical = 16.dp))) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
        }
    }
}
