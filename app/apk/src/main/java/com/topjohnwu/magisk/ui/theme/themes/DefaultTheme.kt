package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.ui.theme.ThemeOption
import com.topjohnwu.magisk.core.R as CoreR

internal val DefaultThemeDefinition = ThemeDefinition(
    option = ThemeOption.Default,
    labelRes = CoreR.string.theme_option_default_dynamic,
    seed = {
        ThemeSeed(
            lightPrimary = Color(0xFF006C5B),
            darkPrimary = Color(0xFF6FDCC7),
            lightSecondary = Color(0xFF6C5D00),
            darkSecondary = Color(0xFFE4C85B),
            lightTertiary = Color(0xFF984A3A),
            darkTertiary = Color(0xFFFFB4A2),
            lightSurface = Color(0xFFFBFDF8),
            darkSurface = Color(0xFF0D1412),
            lightOnSurface = Color(0xFF18201D),
            darkOnSurface = Color(0xFFDDE5E1),
            lightError = Color(0xFFBA1A1A),
            darkError = Color(0xFFFFB4AB)
        )
    }
)
