package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import com.topjohnwu.magisk.ui.theme.ThemeOption
import com.topjohnwu.magisk.core.R as CoreR

internal val SungJinWooThemeDefinition = ThemeDefinition(
    option = ThemeOption.SungJinWoo,
    labelRes = CoreR.string.theme_option_sung_jinwoo,
    seed = {
        ThemeSeed(
            lightPrimary = Color(0xFF5945A8),
            darkPrimary = Color(0xFFC7BFFF),
            lightSecondary = Color(0xFF5F5B71),
            darkSecondary = Color(0xFFC9C3DC),
            lightTertiary = Color(0xFF006C5D),
            darkTertiary = Color(0xFF72DCCB),
            lightSurface = Color(0xFFFCF8FF),
            darkSurface = Color(0xFF12111A),
            lightOnSurface = Color(0xFF1D1A24),
            darkOnSurface = Color(0xFFE6E0EC),
            lightError = Color(0xFFBA1A1A),
            darkError = Color(0xFFFFB4AB)
        )
    }
)
