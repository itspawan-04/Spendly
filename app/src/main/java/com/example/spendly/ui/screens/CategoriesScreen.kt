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
import androidx.compose.material3.AlertDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: SpendlyViewModel,
    onBack: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📁") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showAdd = true }) { Text("Add") }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(categories, key = { it.id }) { cat ->
                CategoryRow(cat, onArchive = { viewModel.archiveCategory(cat.id) })
            }
        }
    }
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New category") },
            text = {
                Column {
                    OutlinedTextField(
                        name,
                        { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = spendlyOutlinedTextFieldColors(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        emoji,
                        { emoji = it.take(4) },
                        label = { Text("Emoji") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = spendlyOutlinedTextFieldColors(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCategory(name, emoji, 0xFF4F46E5L) {
                        if (it == null) {
                            showAdd = false
                            name = ""
                            emoji = "📁"
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun CategoryRow(cat: CategoryEntity, onArchive: () -> Unit) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text("${cat.iconEmoji} ${cat.name}", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onArchive) { Text("Archive") }
    }
}
