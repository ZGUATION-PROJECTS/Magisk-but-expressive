package com.topjohnwu.magisk.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MagiskCard(
    modifier: Modifier = Modifier,
    shape: Shape = MagiskComponentDefaults.CardShape,
    containerColor: Color = MagiskComponentDefaults.CardContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = MagiskComponentDefaults.CardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun MagiskOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = MagiskComponentDefaults.CardShape,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.outlinedCardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )

    if (onClick == null) {
        OutlinedCard(
            modifier = modifier,
            shape = shape,
            colors = colors,
            content = content
        )
    } else {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            content = content
        )
    }
}

@Composable
fun MagiskElevatedPanel(
    modifier: Modifier = Modifier,
    shape: Shape = MagiskComponentDefaults.PanelShape,
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 0.dp,
    color: Color = MagiskComponentDefaults.PanelContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = MagiskComponentDefaults.CardBorder,
        content = content
    )
}

@Composable
fun MagiskActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 72.dp,
    content: @Composable RowScope.() -> Unit
) {
    MagiskCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            content = content
        )
    }
}
