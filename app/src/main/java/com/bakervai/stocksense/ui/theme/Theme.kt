package com.bakervai.stocksense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StockSenseColorScheme = darkColorScheme(
    primary = IndigoAccent,
    secondary = IndigoLight,
    tertiary = AccentGreen,
    background = SlateBg,
    surface = SlateCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = SlateCard,
    outline = SlateBorder
)

@Composable
fun StockSenseTheme(
    darkTheme: Boolean = true, // Force Dark theme for the premium Dark store vibe
    dynamicColor: Boolean = false, // Disable dynamic colors so our SaaS branding remains persistent
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StockSenseColorScheme,
        typography = Typography,
        content = content
    )
}
