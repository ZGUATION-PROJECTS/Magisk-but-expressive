package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.ui.theme.ThemeOption
import com.topjohnwu.magisk.core.R as CoreR

internal val MemChoThemeDefinition = ThemeDefinition(
    option = ThemeOption.MemCho,
    labelRes = CoreR.string.theme_option_memcho,
    seed = {
        ThemeSeed(
            lightPrimary = Color(0xFF725C00),
            darkPrimary = Color(0xFFE8C75D),
            lightSecondary = Color(0xFF53643B),
            darkSecondary = Color(0xFFBBCD99),
            lightTertiary = Color(0xFF006A62),
            darkTertiary = Color(0xFF65D9CE),
            lightSurface = Color(0xFFFFFBF0),
            darkSurface = Color(0xFF17130A),
            lightOnSurface = Color(0xFF201B10),
            darkOnSurface = Color(0xFFEAE1D0),
            lightError = Color(0xFFBA1A1A),
            darkError = Color(0xFFFFB4AB)
        )
    }
)
