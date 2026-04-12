package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spendly.data.db.ExpenseEntity
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.theme.spendlyOutlinedTextFieldColors
import com.example.spendly.util.parseAmountToMinor
import java.time.LocalDate
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    viewModel: SpendlyViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    var entity by remember { mutableStateOf<ExpenseEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var showDelete by remember { mutableStateOf(false) }
    LaunchedEffect(expenseId) {
        val e = viewModel.getExpense(expenseId)
        entity = e
        if (e != null) {
            title = e.title
            amount = (e.amountMinor / 100.0).toString()
            notes = e.notes
            dateText = e.date.toString()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDelete = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
        ) {
            val current = entity
            if (current == null) {
                Text("Loading…")
            } else {
                OutlinedTextField(
                    title,
                    { title = it.take(80) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = spendlyOutlinedTextFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    amount,
                    { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = spendlyOutlinedTextFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    dateText,
                    { dateText = it },
                    label = { Text("Date (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = spendlyOutlinedTextFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    notes,
                    { notes = it.take(500) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = spendlyOutlinedTextFieldColors(),
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val minor = parseAmountToMinor(amount) ?: return@Button
                        val d = try {
                            LocalDate.parse(dateText)
                        } catch (_: DateTimeParseException) {
                            return@Button
                        }
                        viewModel.updateExpense(
                            current.copy(
                                title = title,
                                amountMinor = minor,
                                notes = notes,
                                dateEpochDay = d.toEpochDay(),
                            ),
                        ) { err ->
                            if (err == null) onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save changes") }
            }
        }
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete expense?") },
            text = { Text("It will move to trash for 30 days (simulated in this build).") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(expenseId)
                    showDelete = false
                    onDeleted()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            },
        )
    }
}
