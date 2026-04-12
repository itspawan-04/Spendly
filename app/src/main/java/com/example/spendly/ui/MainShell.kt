package com.example.spendly.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.data.repository.toYyyyMmInt
import com.example.spendly.ui.navigation.SpendlyRoutes
import com.example.spendly.ui.screens.AddExpenseScreen
import com.example.spendly.ui.screens.AnalyticsScreen
import com.example.spendly.ui.screens.CalendarScreen
import com.example.spendly.ui.screens.DashboardScreen
import com.example.spendly.ui.screens.SettingsScreen
import java.time.YearMonth

@Composable
fun MainShell(
    viewModel: SpendlyViewModel,
    prefs: UserPreferences,
    rootNav: NavHostController,
) {
    val tab by viewModel.mainTabIndex.collectAsStateWithLifecycle()
    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp,
            ) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { viewModel.selectMainTab(0) },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navItemColors,
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { viewModel.selectMainTab(1) },
                    icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    colors = navItemColors,
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { viewModel.selectMainTab(2) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                    label = { Text("Add") },
                    colors = navItemColors,
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { viewModel.selectMainTab(3) },
                    icon = { Icon(Icons.Outlined.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    colors = navItemColors,
                    alwaysShowLabel = false,
                )
                NavigationBarItem(
                    selected = tab == 4,
                    onClick = { viewModel.selectMainTab(4) },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navItemColors,
                    alwaysShowLabel = false,
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    onAddExpense = { viewModel.selectMainTab(2) },
                    onOpenCalendar = { viewModel.selectMainTab(1) },
                    onOpenAnalytics = { viewModel.selectMainTab(3) },
                    onOpenExport = { viewModel.selectMainTab(4) },
                    onOpenExpense = { id -> rootNav.navigate(SpendlyRoutes.expense(id)) },
                    onOpenMonthly = {
                        val ym = YearMonth.now().toYyyyMmInt()
                        rootNav.navigate(SpendlyRoutes.monthly(ym))
                    },
                )
                1 -> CalendarScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    onDaySelected = { epoch -> rootNav.navigate(SpendlyRoutes.daily(epoch)) },
                )
                2 -> AddExpenseScreen(viewModel = viewModel, onSaved = { viewModel.selectMainTab(0) })
                3 -> AnalyticsScreen(viewModel = viewModel, prefs = prefs)
                4 -> SettingsScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    onBudgets = { rootNav.navigate(SpendlyRoutes.Budgets) },
                    onCategories = { rootNav.navigate(SpendlyRoutes.Categories) },
                    onSignedOut = {
                        rootNav.navigate(SpendlyRoutes.Welcome) {
                            popUpTo(SpendlyRoutes.Main) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
