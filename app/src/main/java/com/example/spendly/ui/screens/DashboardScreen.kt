@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.data.db.ExpenseEntity
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.util.formatMinorUnits
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onAddExpense: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenMonthly: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val recent by viewModel.recentExpenses.collectAsStateWithLifecycle()
    val catMap = remember(categories) { categories.associateBy { it.id } }
    val ym = remember { YearMonth.now() }
    var monthTotal by remember { mutableStateOf(0L) }
    var prevTotal by remember { mutableStateOf(0L) }
    LaunchedEffect(ym) {
        monthTotal = viewModel.monthTotal(ym)
        prevTotal = viewModel.previousMonthTotal(ym)
    }
    val budget = prefs.monthlyBudgetMinor
    val pctChange = if (prevTotal == 0L) null else ((monthTotal - prevTotal) * 100.0 / prevTotal).roundToInt()
    val budgetProgress = if (budget != null && budget > 0) {
        (monthTotal.toFloat() / budget.toFloat()).coerceIn(0f, 1.2f)
    } else {
        null
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text(
                text = "Hi, ${prefs.displayName?.ifBlank { "there" } ?: "there"}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Here is your spending snapshot.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        text = ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = formatMinorUnits(monthTotal, prefs.currencyCode),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    pctChange?.let {
                        val up = it >= 0
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = (if (up) "Up " else "Down ") + kotlin.math.abs(it) + "% vs last month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (up) MaterialTheme.colorScheme.error else Color(0xFF16A34A),
                        )
                    }
                    if (budget != null && budget > 0) {
                        Spacer(Modifier.height(16.dp))
                        val remaining = (budget - monthTotal).coerceAtLeast(0)
                        Text(
                            text = "${formatMinorUnits(remaining, prefs.currencyCode)} left in budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { budgetProgress ?: 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickChip(Icons.Outlined.CalendarMonth, "Calendar", Modifier.weight(1f), onOpenCalendar)
                QuickChip(Icons.Outlined.Analytics, "Insights", Modifier.weight(1f), onOpenAnalytics)
                QuickChip(Icons.Outlined.FileDownload, "Export", Modifier.weight(1f), onOpenExport)
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                TextButton(onClick = onOpenMonthly) {
                    Text("See month", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (recent.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No expenses yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onAddExpense) {
                            Text("Add your first expense")
                        }
                    }
                }
            }
        } else {
            items(recent, key = { it.id }) { expense ->
                ExpenseRow(expense, catMap[expense.categoryId], prefs.currencyCode) {
                    onOpenExpense(expense.id)
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuickChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ExpenseEntity,
    category: CategoryEntity?,
    currency: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category?.iconEmoji ?: "💸",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = category?.name ?: "Category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = formatMinorUnits(expense.amountMinor, currency),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
