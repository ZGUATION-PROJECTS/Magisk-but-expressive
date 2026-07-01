package com.topjohnwu.magisk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.ui.theme.themes.Black
import com.topjohnwu.magisk.ui.theme.themes.ThemeCatalog
import com.topjohnwu.magisk.ui.theme.themes.ThemeSeed
import com.topjohnwu.magisk.ui.theme.themes.White
import com.topjohnwu.magisk.ui.theme.themes.blend
import com.topjohnwu.magisk.ui.theme.themes.contentColorFor

@Composable
fun MagiskTheme(
    themeOption: ThemeOption = ThemeOption.selected,
    darkTheme: Boolean = shouldUseDarkTheme(),
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        themeOption == ThemeOption.Default &&
            useDynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> ThemeCatalog.seedFor(themeOption).toColorScheme(darkTheme)
    }.let {
        if (darkTheme && Config.darkTheme == Config.Value.DARK_THEME_AMOLED) {
            it.toAmoledScheme()
        } else {
            it
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MagiskTypography,
        shapes = MagiskShapes,
        content = content
    )
}

@Composable
fun shouldUseDarkTheme(): Boolean {
    return when (Config.darkTheme) {
        Config.Value.DARK_THEME_AMOLED -> true
        -1 -> isSystemInDarkTheme()
        0 -> false
        else -> true
    }
}

private val MagiskShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

private val BaseTypography = Typography()

private val MagiskTypography = BaseTypography.copy(
    headlineMedium = BaseTypography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

private fun ThemeSeed.toColorScheme(darkTheme: Boolean): ColorScheme {
    val primary = if (darkTheme) darkPrimary else lightPrimary
    val secondary = if (darkTheme) darkSecondary else lightSecondary
    val tertiary = if (darkTheme) darkTertiary else lightTertiary
    val surface = if (darkTheme) darkSurface else lightSurface
    val onSurface = if (darkTheme) darkOnSurface else lightOnSurface
    val error = if (darkTheme) darkError else lightError
    val target = if (darkTheme) Black else White
    val opposite = if (darkTheme) White else Black

    val primaryContainer = blend(primary, target, if (darkTheme) 0.42f else 0.78f)
    val secondaryContainer = blend(secondary, target, if (darkTheme) 0.38f else 0.74f)
    val tertiaryContainer = blend(tertiary, target, if (darkTheme) 0.40f else 0.76f)
    val errorContainer = blend(error, target, if (darkTheme) 0.42f else 0.78f)
    val surfaceVariant = blend(surface, opposite, if (darkTheme) 0.08f else 0.05f)

    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = contentColorFor(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = contentColorFor(primaryContainer),
            secondary = secondary,
            onSecondary = contentColorFor(secondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = contentColorFor(secondaryContainer),
            tertiary = tertiary,
            onTertiary = contentColorFor(tertiary),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = contentColorFor(tertiaryContainer),
            error = error,
            onError = contentColorFor(error),
            errorContainer = errorContainer,
            onErrorContainer = contentColorFor(errorContainer),
            background = surface,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = blend(onSurface, surface, 0.30f),
            outline = blend(onSurface, surface, 0.58f),
            outlineVariant = blend(onSurface, surface, 0.76f),
            inverseSurface = blend(surface, opposite, 0.82f),
            inverseOnSurface = contentColorFor(blend(surface, opposite, 0.82f)),
            inversePrimary = blend(primary, opposite, 0.28f),
            surfaceTint = primary,
            scrim = Black
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = contentColorFor(primary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = contentColorFor(primaryContainer),
            secondary = secondary,
            onSecondary = contentColorFor(secondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = contentColorFor(secondaryContainer),
            tertiary = tertiary,
            onTertiary = contentColorFor(tertiary),
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = contentColorFor(tertiaryContainer),
            error = error,
            onError = contentColorFor(error),
            errorContainer = errorContainer,
            onErrorContainer = contentColorFor(errorContainer),
            background = surface,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = blend(onSurface, surface, 0.30f),
            outline = blend(onSurface, surface, 0.58f),
            outlineVariant = blend(onSurface, surface, 0.76f),
            inverseSurface = blend(surface, opposite, 0.78f),
            inverseOnSurface = contentColorFor(blend(surface, opposite, 0.78f)),
            inversePrimary = blend(primary, opposite, 0.18f),
            surfaceTint = primary,
            scrim = Black
        )
    }
}

private fun ColorScheme.toAmoledScheme(): ColorScheme = copy(
    background = Black,
    surface = Black,
    surfaceVariant = Color(0xFF101010),
    surfaceDim = Black,
    surfaceBright = Color(0xFF171717),
    surfaceContainerLowest = Black,
    surfaceContainerLow = Color(0xFF050505),
    surfaceContainer = Color(0xFF090909),
    surfaceContainerHigh = Color(0xFF0E0E0E),
    surfaceContainerHighest = Color(0xFF141414),
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF141414),
    scrim = Black
)
