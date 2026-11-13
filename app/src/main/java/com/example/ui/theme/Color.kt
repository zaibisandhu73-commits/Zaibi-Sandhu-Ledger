package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class LedgerThemePalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val primaryBorder: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val emeraldReceivable: Color,
    val emeraldReceivableSoft: Color,
    val emeraldReceivableBorder: Color,
    val emeraldDarkText: Color,
    val rubyPayable: Color,
    val rubyPayableSoft: Color,
    val rubyPayableBorder: Color,
    val rubyDarkText: Color,
    val background: Color,
    val surface: Color,
    val surfaceSubtle: Color,
    val border: Color,
    val borderStrong: Color,
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkMuted: Color
)

enum class AppThemePreset(
    val key: String,
    val title: String,
    val subtitle: String,
    val isDark: Boolean,
    val palette: LedgerThemePalette
) {
    SAAS_INDIGO(
        key = "SAAS_INDIGO",
        title = "SaaS Indigo Glass",
        subtitle = "Clean Slate & Indigo light workspace",
        isDark = false,
        palette = LedgerThemePalette(
            primary = Color(0xFF4F46E5),
            primaryDark = Color(0xFF4338CA),
            primaryContainer = Color(0xFFEEF2FF),
            primaryBorder = Color(0xFFC7D2FE),
            onPrimaryContainer = Color(0xFF312E81),
            secondary = Color(0xFF0284C7),
            secondaryContainer = Color(0xFFE0F2FE),
            onSecondaryContainer = Color(0xFF0C4A6E),
            emeraldReceivable = Color(0xFF059669),
            emeraldReceivableSoft = Color(0xFFECFDF5),
            emeraldReceivableBorder = Color(0xFFA7F3D0),
            emeraldDarkText = Color(0xFF065F46),
            rubyPayable = Color(0xFFE11D48),
            rubyPayableSoft = Color(0xFFFFF1F2),
            rubyPayableBorder = Color(0xFFFECDD3),
            rubyDarkText = Color(0xFF9F1239),
            background = Color(0xFFF8FAFC),
            surface = Color(0xFFFFFFFF),
            surfaceSubtle = Color(0xFFF1F5F9),
            border = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            inkPrimary = Color(0xFF0F172A),
            inkSecondary = Color(0xFF475569),
            inkMuted = Color(0xFF64748B)
        )
    ),
    MIDNIGHT_DARK(
        key = "MIDNIGHT_DARK",
        title = "Midnight Obsidian",
        subtitle = "Sleek dark slate with neon glass accents",
        isDark = true,
        palette = LedgerThemePalette(
            primary = Color(0xFF6366F1),
            primaryDark = Color(0xFF4F46E5),
            primaryContainer = Color(0xFF1E1B4B),
            primaryBorder = Color(0xFF3730A3),
            onPrimaryContainer = Color(0xFFE0E7FF),
            secondary = Color(0xFF38BDF8),
            secondaryContainer = Color(0xFF082F49),
            onSecondaryContainer = Color(0xFFBAE6FD),
            emeraldReceivable = Color(0xFF34D399),
            emeraldReceivableSoft = Color(0xFF062E24),
            emeraldReceivableBorder = Color(0xFF059669),
            emeraldDarkText = Color(0xFFD1FAE5),
            rubyPayable = Color(0xFFFB7185),
            rubyPayableSoft = Color(0xFF3B0A19),
            rubyPayableBorder = Color(0xFFE11D48),
            rubyDarkText = Color(0xFFFFE4E6),
            background = Color(0xFF0B0F19),
            surface = Color(0xFF111827),
            surfaceSubtle = Color(0xFF1E293B),
            border = Color(0xFF1F2937),
            borderStrong = Color(0xFF334155),
            inkPrimary = Color(0xFFF8FAFC),
            inkSecondary = Color(0xFFCBD5E1),
            inkMuted = Color(0xFF94A3B8)
        )
    ),
    EMERALD_MINT(
        key = "EMERALD_MINT",
        title = "Emerald Fintech",
        subtitle = "Fresh mint alabaster & deep teal banking look",
        isDark = false,
        palette = LedgerThemePalette(
            primary = Color(0xFF0D9488),
            primaryDark = Color(0xFF0F766E),
            primaryContainer = Color(0xFFCCFBF1),
            primaryBorder = Color(0xFF99F6E4),
            onPrimaryContainer = Color(0xFF134E4A),
            secondary = Color(0xFF059669),
            secondaryContainer = Color(0xFFD1FAE5),
            onSecondaryContainer = Color(0xFF064E3B),
            emeraldReceivable = Color(0xFF059669),
            emeraldReceivableSoft = Color(0xFFECFDF5),
            emeraldReceivableBorder = Color(0xFF6EE7B7),
            emeraldDarkText = Color(0xFF065F46),
            rubyPayable = Color(0xFFE11D48),
            rubyPayableSoft = Color(0xFFFFF1F2),
            rubyPayableBorder = Color(0xFFFECDD3),
            rubyDarkText = Color(0xFF9F1239),
            background = Color(0xFFF2FBF7),
            surface = Color(0xFFFFFFFF),
            surfaceSubtle = Color(0xFFE6F6EF),
            border = Color(0xFFD1EAE0),
            borderStrong = Color(0xFFA7D7C5),
            inkPrimary = Color(0xFF062C22),
            inkSecondary = Color(0xFF2D5A4C),
            inkMuted = Color(0xFF527E70)
        )
    ),
    SUNSET_WARM(
        key = "SUNSET_WARM",
        title = "Warm Terracotta",
        subtitle = "Warm paper ivory & terracotta editorial tone",
        isDark = false,
        palette = LedgerThemePalette(
            primary = Color(0xFFD97706),
            primaryDark = Color(0xFFB45309),
            primaryContainer = Color(0xFFFEF3C7),
            primaryBorder = Color(0xFFFDE68A),
            onPrimaryContainer = Color(0xFF78350F),
            secondary = Color(0xFFEA580C),
            secondaryContainer = Color(0xFFFFEDD5),
            onSecondaryContainer = Color(0xFF7C2D12),
            emeraldReceivable = Color(0xFF059669),
            emeraldReceivableSoft = Color(0xFFECFDF5),
            emeraldReceivableBorder = Color(0xFFA7F3D0),
            emeraldDarkText = Color(0xFF065F46),
            rubyPayable = Color(0xFFE11D48),
            rubyPayableSoft = Color(0xFFFFF1F2),
            rubyPayableBorder = Color(0xFFFECDD3),
            rubyDarkText = Color(0xFF9F1239),
            background = Color(0xFFFAF6F0),
            surface = Color(0xFFFFFDF9),
            surfaceSubtle = Color(0xFFF3EDE2),
            border = Color(0xFFE6DEC8),
            borderStrong = Color(0xFFD6C9B0),
            inkPrimary = Color(0xFF1C1917),
            inkSecondary = Color(0xFF57534E),
            inkMuted = Color(0xFF78716C)
        )
    ),
    OCEAN_BREEZE(
        key = "OCEAN_BREEZE",
        title = "Oceanic Blue",
        subtitle = "Cool arctic frost & royal sky neobank style",
        isDark = false,
        palette = LedgerThemePalette(
            primary = Color(0xFF0284C7),
            primaryDark = Color(0xFF0369A1),
            primaryContainer = Color(0xFFE0F2FE),
            primaryBorder = Color(0xFFBAE6FD),
            onPrimaryContainer = Color(0xFF0C4A6E),
            secondary = Color(0xFF2563EB),
            secondaryContainer = Color(0xFFDBEAFE),
            onSecondaryContainer = Color(0xFF1E3A8A),
            emeraldReceivable = Color(0xFF059669),
            emeraldReceivableSoft = Color(0xFFECFDF5),
            emeraldReceivableBorder = Color(0xFFA7F3D0),
            emeraldDarkText = Color(0xFF065F46),
            rubyPayable = Color(0xFFE11D48),
            rubyPayableSoft = Color(0xFFFFF1F2),
            rubyPayableBorder = Color(0xFFFECDD3),
            rubyDarkText = Color(0xFF9F1239),
            background = Color(0xFFF0F7FF),
            surface = Color(0xFFFFFFFF),
            surfaceSubtle = Color(0xFFE2F0FE),
            border = Color(0xFFCCE3FD),
            borderStrong = Color(0xFF93C5FD),
            inkPrimary = Color(0xFF082F49),
            inkSecondary = Color(0xFF335C81),
            inkMuted = Color(0xFF5B82A6)
        )
    );

    companion object {
        fun fromKey(key: String?): AppThemePreset {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SAAS_INDIGO
        }
    }
}

object LedgerThemeState {
    var currentPreset by mutableStateOf(AppThemePreset.SAAS_INDIGO)
}

// Reactive Theme Tokens (automatically trigger Compose recomposition when LedgerThemeState.currentPreset changes)
val SaasIndigoPrimary: Color get() = LedgerThemeState.currentPreset.palette.primary
val SaasIndigoDark: Color get() = LedgerThemeState.currentPreset.palette.primaryDark
val SaasIndigoContainer: Color get() = LedgerThemeState.currentPreset.palette.primaryContainer
val SaasIndigoBorder: Color get() = LedgerThemeState.currentPreset.palette.primaryBorder
val OnSaasIndigoContainer: Color get() = LedgerThemeState.currentPreset.palette.onPrimaryContainer

val SaasSkySecondary: Color get() = LedgerThemeState.currentPreset.palette.secondary
val SaasSkyContainer: Color get() = LedgerThemeState.currentPreset.palette.secondaryContainer
val OnSaasSkyContainer: Color get() = LedgerThemeState.currentPreset.palette.onSecondaryContainer

// Semantic Financial Colors
val EmeraldReceivable: Color get() = LedgerThemeState.currentPreset.palette.emeraldReceivable
val EmeraldReceivableSoft: Color get() = LedgerThemeState.currentPreset.palette.emeraldReceivableSoft
val EmeraldReceivableBorder: Color get() = LedgerThemeState.currentPreset.palette.emeraldReceivableBorder
val EmeraldDarkText: Color get() = LedgerThemeState.currentPreset.palette.emeraldDarkText

val RubyPayable: Color get() = LedgerThemeState.currentPreset.palette.rubyPayable
val RubyPayableSoft: Color get() = LedgerThemeState.currentPreset.palette.rubyPayableSoft
val RubyPayableBorder: Color get() = LedgerThemeState.currentPreset.palette.rubyPayableBorder
val RubyDarkText: Color get() = LedgerThemeState.currentPreset.palette.rubyDarkText

// Surfaces & Borders
val SaasBackground: Color get() = LedgerThemeState.currentPreset.palette.background
val SaasSurface: Color get() = LedgerThemeState.currentPreset.palette.surface
val SaasSurfaceSubtle: Color get() = LedgerThemeState.currentPreset.palette.surfaceSubtle
val SaasBorder: Color get() = LedgerThemeState.currentPreset.palette.border
val SaasBorderStrong: Color get() = LedgerThemeState.currentPreset.palette.borderStrong
val SaasInkPrimary: Color get() = LedgerThemeState.currentPreset.palette.inkPrimary
val SaasInkSecondary: Color get() = LedgerThemeState.currentPreset.palette.inkSecondary
val SaasInkMuted: Color get() = LedgerThemeState.currentPreset.palette.inkMuted

// Backward-compatible alias used in legacy references
val NavyPrimary: Color get() = SaasIndigoPrimary
