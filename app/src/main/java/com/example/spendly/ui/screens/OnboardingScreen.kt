package com.example.spendly.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spendly.ui.theme.spendlyOutlinedTextFieldColors
import com.example.spendly.util.parseAmountToMinor

@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onComplete: (String, String, Long?, Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var budgetText by remember { mutableStateOf("") }
    var notifications by remember { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Text(
            "Welcome to Spendly",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tell us a bit about you. You can skip optional steps.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = currency,
            onValueChange = { currency = it.uppercase() },
            label = { Text("Home currency (ISO code)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = budgetText,
            onValueChange = { budgetText = it },
            label = { Text("Monthly budget (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Leave blank to skip") },
            shape = MaterialTheme.shapes.medium,
            colors = spendlyOutlinedTextFieldColors(),
        )
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Enable reminders",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {
                val budgetMinor = parseAmountToMinor(budgetText)
                onComplete(name, currency.ifBlank { "USD" }, budgetMinor, notifications)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp),
        ) { Text("Continue") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) { Text("Skip for now") }
    }
}
