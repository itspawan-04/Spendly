package com.example.spendly.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.theme.spendlyOutlinedTextFieldColors
import com.example.spendly.util.ExportHelper
import com.example.spendly.util.parseAmountToMinor
import java.time.YearMonth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onBudgets: () -> Unit,
    onCategories: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var monthlyBudget by rememberSaveable { mutableStateOf("") }
    var dailyLimit by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = prefs.sessionEmail.orEmpty().ifBlank { "Local session" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = "Budgets",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly total (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = spendlyOutlinedTextFieldColors(),
                )
                OutlinedTextField(
                    value = dailyLimit,
                    onValueChange = { dailyLimit = it },
                    label = { Text("Daily limit (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = spendlyOutlinedTextFieldColors(),
                )
                FilledTonalButton(
                    onClick = {
                        viewModel.setMonthlyBudget(parseAmountToMinor(monthlyBudget))
                        viewModel.setDailyLimit(parseAmountToMinor(dailyLimit))
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) { Text("Save budget preferences") }
            }
        }

        Text(
            text = "Tools",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onBudgets,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) { Text("Category budgets") }
                FilledTonalButton(
                    onClick = onCategories,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) { Text("Manage categories") }
            }
        }

        Text(
            text = "Export",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val ym = YearMonth.now()
                            val start = ym.atDay(1)
                            val end = ym.atEndOfMonth()
                            val rows = viewModel.expensesForExport(start, end)
                            val cats = viewModel.allCategoriesMap()
                            val uri = ExportHelper.csvUri(context, rows, cats)
                            ExportHelper.share(context, uri, "text/csv")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) { Text("This month · CSV") }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val ym = YearMonth.now()
                            val start = ym.atDay(1)
                            val end = ym.atEndOfMonth()
                            val rows = viewModel.expensesForExport(start, end)
                            val cats = viewModel.allCategoriesMap()
                            val total = rows.sumOf { it.amountMinor }
                            val summary = listOf(
                                "User: ${prefs.displayName.orEmpty()}",
                                "Range: $start – $end",
                                "Total: ${total / 100.0}",
                            )
                            val uri = ExportHelper.pdfUri(
                                context,
                                "Spendly report",
                                rows,
                                cats,
                                summary,
                            )
                            ExportHelper.share(context, uri, "application/pdf")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) { Text("This month · PDF") }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = {
                viewModel.signOut()
                onSignedOut()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Sign out", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
        }
    }
}
