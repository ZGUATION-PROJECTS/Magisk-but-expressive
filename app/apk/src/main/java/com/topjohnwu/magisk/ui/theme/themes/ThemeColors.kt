package com.topjohnwu.magisk.ui.theme.themes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal val White = Color(0xFFFFFFFF)
internal val Black = Color(0xFF000000)

internal fun contentColorFor(color: Color): Color {
    return if (color.luminance() > 0.42f) Black else White
}

internal fun blend(base: Color, overlay: Color, amount: Float): Color {
    val safeAmount = amount.coerceIn(0f, 1f)
    val inverse = 1f - safeAmount
    return Color(
        red = base.red * inverse + overlay.red * safeAmount,
        green = base.green * inverse + overlay.green * safeAmount,
        blue = base.blue * inverse + overlay.blue * safeAmount,
        alpha = 1f
    )
}
