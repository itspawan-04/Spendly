package com.example.spendly.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spendly.data.db.ExpenseEntity
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.util.formatMinorUnits
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    epochDay: Long,
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onBack: () -> Unit,
    onExpense: (Long) -> Unit,
) {
    val date = LocalDate.ofEpochDay(epochDay)
    val expenses by viewModel.observeExpensesForDay(epochDay).collectAsStateWithLifecycle(initialValue = emptyList())
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val catMap = remember(categories) { categories.associateBy { it.id } }
    val total = expenses.sumOf { it.amountMinor }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(date.format(DateTimeFormatter.ofPattern("EEE, MMM d", java.util.Locale.getDefault()))) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item {
                Text(
                    "Total ${formatMinorUnits(total, prefs.currencyCode)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseDayRow(expense, catMap[expense.categoryId], prefs.currencyCode) { onExpense(expense.id) }
            }
        }
    }
}

@Composable
private fun ExpenseDayRow(
    expense: ExpenseEntity,
    category: com.example.spendly.data.db.CategoryEntity?,
    currency: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(category?.iconEmoji ?: "💸", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.padding(8.dp))
        Column(Modifier.weight(1f)) {
            Text(expense.title, style = MaterialTheme.typography.titleMedium)
            Text(category?.name.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(formatMinorUnits(expense.amountMinor, currency), style = MaterialTheme.typography.titleMedium)
    }
}
