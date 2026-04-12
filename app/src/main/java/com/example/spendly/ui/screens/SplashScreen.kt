package com.example.spendly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.preferences.UserPreferences
import com.example.spendly.ui.navigation.SpendlyRoutes
import com.example.spendly.ui.theme.SpendlyIndigo
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(
    prefs: UserPreferences,
    navigate: (String) -> Unit,
) {
    LaunchedEffect(prefs.isLoggedIn, prefs.onboardingComplete) {
        delay(650)
        val target = when {
            !prefs.isLoggedIn -> SpendlyRoutes.Welcome
            !prefs.onboardingComplete -> SpendlyRoutes.Onboarding
            else -> SpendlyRoutes.Main
        }
        navigate(target)
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(SpendlyIndigo),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = Color.White,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Spendly",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Track spending. Stay in control.",
                color = Color(0xFFE0E7FF),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Color.White,
                trackColor = Color(0x66FFFFFF),
            )
        }
    }
}
