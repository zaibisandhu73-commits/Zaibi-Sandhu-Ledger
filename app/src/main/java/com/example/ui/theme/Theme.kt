package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    themePreset: AppThemePreset = LedgerThemeState.currentPreset,
    content: @Composable () -> Unit
) {
    val p = themePreset.palette
    val colorScheme = if (themePreset.isDark) {
        darkColorScheme(
            primary = p.primary,
            onPrimary = Color.White,
            primaryContainer = p.primaryContainer,
            onPrimaryContainer = p.onPrimaryContainer,
            secondary = p.secondary,
            onSecondary = Color.White,
            secondaryContainer = p.secondaryContainer,
            onSecondaryContainer = p.onSecondaryContainer,
            tertiary = p.emeraldReceivable,
            onTertiary = Color.White,
            tertiaryContainer = p.emeraldReceivableSoft,
            onTertiaryContainer = p.emeraldDarkText,
            error = p.rubyPayable,
            onError = Color.White,
            errorContainer = p.rubyPayableSoft,
            onErrorContainer = p.rubyDarkText,
            background = p.background,
            onBackground = p.inkPrimary,
            surface = p.surface,
            onSurface = p.inkPrimary,
            surfaceVariant = p.surfaceSubtle,
            onSurfaceVariant = p.inkSecondary,
            outline = p.border,
            outlineVariant = p.border
        )
    } else {
        lightColorScheme(
            primary = p.primary,
            onPrimary = Color.White,
            primaryContainer = p.primaryContainer,
            onPrimaryContainer = p.onPrimaryContainer,
            secondary = p.secondary,
            onSecondary = Color.White,
            secondaryContainer = p.secondaryContainer,
            onSecondaryContainer = p.onSecondaryContainer,
            tertiary = p.emeraldReceivable,
            onTertiary = Color.White,
            tertiaryContainer = p.emeraldReceivableSoft,
            onTertiaryContainer = p.emeraldDarkText,
            error = p.rubyPayable,
            onError = Color.White,
            errorContainer = p.rubyPayableSoft,
            onErrorContainer = p.rubyDarkText,
            background = p.background,
            onBackground = p.inkPrimary,
            surface = p.surface,
            onSurface = p.inkPrimary,
            surfaceVariant = p.surfaceSubtle,
            onSurfaceVariant = p.inkSecondary,
            outline = p.border,
            outlineVariant = p.border
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
