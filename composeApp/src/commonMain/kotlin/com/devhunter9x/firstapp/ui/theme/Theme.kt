package com.devhunter9x.firstapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
        darkColorScheme(
                primary = Primary,
                onPrimary = OnPrimary,
                secondary = Secondary,
                onSecondary = OnSecondary,
                tertiary = Tertiary,
                onTertiary = OnTertiary,
                background = Background,
                onBackground = OnBackground,
                surface = Surface,
                onSurface = OnSurface,
                surfaceVariant = SurfaceVariant,
                onSurfaceVariant = OnSurfaceVariant,
                error = ErrorColor,
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
