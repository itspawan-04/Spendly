package com.example.spendly.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spendly.ui.navigation.SpendlyRoutes
import com.example.spendly.ui.screens.BudgetsScreen
import com.example.spendly.ui.screens.CategoriesScreen
import com.example.spendly.ui.screens.DailyScreen
import com.example.spendly.ui.screens.ExpenseDetailScreen
import com.example.spendly.ui.screens.MonthlyScreen
import com.example.spendly.ui.screens.OnboardingScreen
import com.example.spendly.ui.screens.SignInScreen
import com.example.spendly.ui.screens.SignUpScreen
import com.example.spendly.ui.screens.SplashRoute
import com.example.spendly.ui.screens.WelcomeScreen
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.YearMonth

@Composable
fun SpendlyApp() {
    SpendlyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
        val viewModel: SpendlyViewModel = viewModel()
        val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SpendlyRoutes.Splash,
        ) {
            composable(SpendlyRoutes.Splash) {
                SplashRoute(prefs = prefs) { target ->
                    navController.navigate(target) {
                        popUpTo(SpendlyRoutes.Splash) { inclusive = true }
                    }
                }
            }
            composable(SpendlyRoutes.Welcome) {
                WelcomeScreen(
                    onSignIn = { navController.navigate(SpendlyRoutes.SignIn) },
                    onSignUp = { navController.navigate(SpendlyRoutes.SignUp) },
                    onTryLocal = {
                        viewModel.continueLocal()
                        navController.navigate(SpendlyRoutes.Onboarding) {
                            popUpTo(SpendlyRoutes.Welcome) { inclusive = true }
                        }
                    },
                )
            }
            composable(SpendlyRoutes.SignIn) {
                SignInScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(SpendlyRoutes.Onboarding) {
                            popUpTo(SpendlyRoutes.Welcome) { inclusive = true }
                        }
                    },
                    signIn = { email, password, cb -> viewModel.signIn(email, password, cb) },
                )
            }
            composable(SpendlyRoutes.SignUp) {
                SignUpScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(SpendlyRoutes.Onboarding) {
                            popUpTo(SpendlyRoutes.Welcome) { inclusive = true }
                        }
                    },
                    signUp = { email, password, cb -> viewModel.signUp(email, password, cb) },
                )
            }
            composable(SpendlyRoutes.Onboarding) {
                if (prefs.onboardingComplete) {
                    LaunchedEffect(Unit) {
                        navController.navigate(SpendlyRoutes.Main) {
                            popUpTo(SpendlyRoutes.Onboarding) { inclusive = true }
                        }
                    }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    OnboardingScreen(
                        onSkip = {
                            viewModel.skipOnboarding()
                            navController.navigate(SpendlyRoutes.Main) {
                                popUpTo(SpendlyRoutes.Onboarding) { inclusive = true }
                            }
                        },
                        onComplete = { name, currency, budgetMinor, notifications ->
                            viewModel.completeOnboarding(name, currency, budgetMinor, notifications)
                            navController.navigate(SpendlyRoutes.Main) {
                                popUpTo(SpendlyRoutes.Onboarding) { inclusive = true }
                            }
                        },
                    )
                }
            }
            composable(SpendlyRoutes.Main) {
                MainShell(
                    viewModel = viewModel,
                    prefs = prefs,
                    rootNav = navController,
                )
            }
            composable(
                route = SpendlyRoutes.Expense,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                ExpenseDetailScreen(
                    expenseId = id,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable(
                route = SpendlyRoutes.Daily,
                arguments = listOf(navArgument("epoch") { type = NavType.LongType }),
            ) { entry ->
                val epoch = entry.arguments?.getLong("epoch") ?: return@composable
                DailyScreen(
                    epochDay = epoch,
                    viewModel = viewModel,
                    prefs = prefs,
                    onBack = { navController.popBackStack() },
                    onExpense = { eid -> navController.navigate(SpendlyRoutes.expense(eid)) },
                )
            }
            composable(
                route = SpendlyRoutes.Monthly,
                arguments = listOf(navArgument("ym") { type = NavType.IntType }),
            ) { entry ->
                val ymInt = entry.arguments?.getInt("ym") ?: return@composable
                val ym = YearMonth.of(ymInt / 100, ymInt % 100)
                MonthlyScreen(
                    ym = ym,
                    viewModel = viewModel,
                    prefs = prefs,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SpendlyRoutes.Budgets) {
                BudgetsScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(SpendlyRoutes.Categories) {
                CategoriesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        }
    }
}
