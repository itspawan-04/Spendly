package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.theme.spendlyOutlinedTextFieldColors
import com.example.spendly.util.parseAmountToMinor
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    onBack: () -> Unit,
) {
    val ym = remember { YearMonth.now() }
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val budgets by viewModel.observeCategoryBudgets(ym).collectAsStateWithLifecycle(initialValue = emptyList())
    val budgetByCat = remember(budgets) { budgets.associateBy { it.categoryId } }
    val drafts = remember { mutableStateMapOf<Long, String>() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category budgets") },
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
                    "Set limits for ${ym.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))}. Alerts at 80% / 100% are shown on the dashboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
            }
            items(categories, key = { it.id }) { cat ->
                val existing = budgetByCat[cat.id]
                val text = drafts[cat.id] ?: existing?.let { (it.amountMinor / 100.0).toString() }.orEmpty()
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text("${cat.iconEmoji} ${cat.name}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { drafts[cat.id] = it },
                        label = { Text("Budget (${prefs.currencyCode})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = spendlyOutlinedTextFieldColors(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val minor = parseAmountToMinor(drafts[cat.id] ?: text) ?: return@Button
                            viewModel.setCategoryBudget(cat.id, ym, minor, rollover = false)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Save") }
                }
            }
        }
    }
}
