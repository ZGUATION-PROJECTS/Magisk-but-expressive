package com.topjohnwu.magisk.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object MagiskListItemDefaults {
    val ContainerColor: Color
        @Composable get() = Color.Transparent

    val SelectedContainerColor: Color
        @Composable get() = MagiskComponentDefaults.SelectedContainer
}

@Composable
fun MagiskListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    }

    ListItem(
        modifier = clickModifier.fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MagiskListItemDefaults.SelectedContainerColor
            } else {
                MagiskListItemDefaults.ContainerColor
            },
            headlineColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        leadingContent = leadingIcon?.let {
            { MagiskIconBadge(icon = it) }
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        trailingContent = trailingContent?.let {
            {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    content = it
                )
            }
        }
    )
}

@Composable
fun MagiskSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    MagiskListItem(
        title = title,
        subtitle = subtitle,
        leadingIcon = leadingIcon,
        enabled = enabled,
        modifier = modifier,
        onClick = { onCheckedChange(!checked) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                thumbContent = if (checked) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                } else {
                    null
                }
            )
        }
    )
}

@Composable
fun MagiskExpandableListItem(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    headerTrailingContent: (@Composable RowScope.() -> Unit)? = null,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    MagiskCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            MagiskListItem(
                title = title,
                subtitle = subtitle,
                leadingIcon = leadingIcon,
                onClick = onClick,
                trailingContent = {
                    headerTrailingContent?.invoke(this)
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = expandedContent
                )
            }
        }
    }
}
