package com.example.spendly.ui.navigation

object SpendlyRoutes {
    const val Splash = "splash"
    const val Welcome = "welcome"
    const val SignIn = "sign_in"
    const val SignUp = "sign_up"
    const val Onboarding = "onboarding"
    const val Main = "main"
    const val Expense = "expense/{id}"
    const val Daily = "daily/{epoch}"
    const val Monthly = "monthly/{ym}"
    const val Budgets = "budgets"
    const val Categories = "categories"

    fun expense(id: Long) = "expense/$id"
    fun daily(epochDay: Long) = "daily/$epochDay"
    fun monthly(ym: Int) = "monthly/$ym"
}
