package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.ui.theme.ThemeOption
import com.topjohnwu.magisk.core.R as CoreR

internal val RubyThemeDefinition = ThemeDefinition(
    option = ThemeOption.Ruby,
    labelRes = CoreR.string.theme_option_ruby,
    seed = {
        ThemeSeed(
            lightPrimary = Color(0xFFC1185A),
            darkPrimary = Color(0xFFFFB1C8),
            lightSecondary = Color(0xFF7C5260),
            darkSecondary = Color(0xFFEAB8C8),
            lightTertiary = Color(0xFF6F5B00),
            darkTertiary = Color(0xFFE1C866),
            lightSurface = Color(0xFFFFF8FA),
            darkSurface = Color(0xFF191114),
            lightOnSurface = Color(0xFF24191C),
            darkOnSurface = Color(0xFFECE0E3),
            lightError = Color(0xFFBA1A1A),
            darkError = Color(0xFFFFB4AB)
        )
    }
)
