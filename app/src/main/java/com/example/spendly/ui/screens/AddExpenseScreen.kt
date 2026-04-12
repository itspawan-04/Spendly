package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spendly.data.db.CategoryEntity
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.theme.spendlyOutlinedTextFieldColors
import com.example.spendly.util.parseAmountToMinor
import java.time.LocalDate
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: SpendlyViewModel,
    onSaved: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var category by remember { mutableStateOf<CategoryEntity?>(null) }
    var recurrence by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var recurrenceMenu by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Text(
            text = "Add expense",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Log a purchase or bill. You can edit it later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it.take(80) },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = { Text("Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(10.dp))
        LaunchedEffect(categories) {
            if (category == null && categories.isNotEmpty()) {
                category = categories.first()
            }
        }
        ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = it }) {
            OutlinedTextField(
                value = category?.let { "${it.iconEmoji} ${it.name}" }.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = spendlyOutlinedTextFieldColors(),
            )
            ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text("${cat.iconEmoji} ${cat.name}") },
                        onClick = {
                            category = cat
                            menuExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        ExposedDropdownMenuBox(expanded = recurrenceMenu, onExpandedChange = { recurrenceMenu = it }) {
            OutlinedTextField(
                value = when (recurrence) {
                    null -> "None"
                    else -> recurrence!!
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Recurring") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceMenu) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = spendlyOutlinedTextFieldColors(),
            )
            ExposedDropdownMenu(expanded = recurrenceMenu, onDismissRequest = { recurrenceMenu = false }) {
                listOf("None" to null, "Daily" to "DAILY", "Weekly" to "WEEKLY", "Monthly" to "MONTHLY", "Yearly" to "YEARLY").forEach { (label, key) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            recurrence = key
                            recurrenceMenu = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it.take(500) },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                val minor = parseAmountToMinor(amount) ?: run {
                    error = "Enter a valid amount"
                    return@Button
                }
                val parsedDate = try {
                    LocalDate.parse(dateText)
                } catch (_: DateTimeParseException) {
                    error = "Invalid date"
                    return@Button
                }
                val cat = category ?: run {
                    error = "Pick a category"
                    return@Button
                }
                viewModel.addExpense(title, minor, cat.id, parsedDate, notes, recurrence) { err ->
                    error = err
                    if (err == null) onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
        ) { Text("Save expense") }
    }
}
