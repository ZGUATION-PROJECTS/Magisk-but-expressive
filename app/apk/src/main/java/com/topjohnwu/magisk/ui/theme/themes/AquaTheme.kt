package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.ui.theme.ThemeOption
import com.topjohnwu.magisk.core.R as CoreR

internal val AquaThemeDefinition = ThemeDefinition(
    option = ThemeOption.Aqua,
    labelRes = CoreR.string.theme_option_aqua,
    seed = {
        ThemeSeed(
            lightPrimary = Color(0xFF006B8F),
            darkPrimary = Color(0xFF86D2F4),
            lightSecondary = Color(0xFF4E626C),
            darkSecondary = Color(0xFFB6CBD5),
            lightTertiary = Color(0xFF7B5266),
            darkTertiary = Color(0xFFECB8D0),
            lightSurface = Color(0xFFF7FCFF),
            darkSurface = Color(0xFF0B1418),
            lightOnSurface = Color(0xFF171D20),
            darkOnSurface = Color(0xFFDCE4E8),
            lightError = Color(0xFFBA1A1A),
            darkError = Color(0xFFFFB4AB)
        )
    }
)
