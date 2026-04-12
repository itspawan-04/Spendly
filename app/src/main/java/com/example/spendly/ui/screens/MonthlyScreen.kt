package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(
    ym: YearMonth,
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onBack: () -> Unit,
) {
    var breakdown by remember { mutableStateOf<List<Pair<CategoryEntity, Long>>>(emptyList()) }
    var monthTotal by remember { mutableStateOf(0L) }
    var prevTotal by remember { mutableStateOf(0L) }
    LaunchedEffect(ym) {
        breakdown = viewModel.categoryBreakdown(ym)
        monthTotal = viewModel.monthTotal(ym)
        prevTotal = viewModel.previousMonthTotal(ym)
    }
    val max = (breakdown.maxOfOrNull { it.second } ?: 1L).coerceAtLeast(1L)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text(
                    "Total ${formatMinorUnits(monthTotal, prefs.currencyCode)}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                val delta = monthTotal - prevTotal
                Text(
                    "vs last month: ${formatMinorUnits(kotlin.math.abs(delta), prefs.currencyCode)} ${if (delta >= 0) "more" else "less"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text("Category breakdown", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(breakdown, key = { it.first.id }) { (cat, total) ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.iconEmoji, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.width(8.dp))
                            Text(cat.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(formatMinorUnits(total, prefs.currencyCode), style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { total.toFloat() / max.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                    }
                }
            }
        }
    }
}
