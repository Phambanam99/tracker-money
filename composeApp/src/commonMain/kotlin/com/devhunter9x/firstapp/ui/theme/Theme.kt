package com.devhunter9x.firstapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
        darkColorScheme(
                primary = PrimaryAmber,
                onPrimary = OnPrimary,
                primaryContainer = PrimaryAmberVariant,
                secondary = SurfaceSlateVariant,
                onSecondary = OnSurface,
                background = BackgroundSlate,
                onBackground = OnBackground,
                surface = SurfaceSlate,
                onSurface = OnSurface,
                surfaceVariant = SurfaceSlateVariant,
                onSurfaceVariant = OnSurfaceVariant,
                error = ErrorRed,
                outline = OnSurfaceVariant.copy(alpha = 0.5f)
        )

@Composable
fun TrackerMoneyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colorScheme = DarkColorScheme,
            // Typography and shapes can be customized here later if needed
            content = content
    )
}
