package com.topjohnwu.magisk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class MagiskDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val destructive: Boolean = false
)

@Composable
fun MagiskDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    textContent: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    confirmAction: MagiskDialogAction? = null,
    dismissAction: MagiskDialogAction? = null
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MagiskComponentDefaults.PrimaryIconTint
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MagiskComponentDefaults.PrimaryText
            )
        },
        text = when {
            textContent != null -> textContent
            text != null -> {
                {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MagiskComponentDefaults.SecondaryText
                    )
                }
            }
            else -> null
        },
        confirmButton = {
            if (confirmAction != null) {
                Button(
                    onClick = confirmAction.onClick,
                    enabled = confirmAction.enabled,
                    colors = if (confirmAction.destructive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(confirmAction.text)
                }
            }
        },
        dismissButton = {
            if (dismissAction != null) {
                TextButton(
                    onClick = dismissAction.onClick,
                    enabled = dismissAction.enabled
                ) {
                    Text(dismissAction.text)
                }
            }
        },
        shape = MagiskComponentDefaults.CardShape,
        containerColor = MagiskComponentDefaults.PanelContainer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagiskBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = MagiskComponentDefaults.CardShape,
        containerColor = MagiskComponentDefaults.PanelContainer,
        content = content
    )
}

@Composable
fun MagiskDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .widthIn(min = 180.dp)
            .heightIn(max = 480.dp),
        shape = MagiskComponentDefaults.ControlShape,
        containerColor = MagiskComponentDefaults.PanelContainer,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        content = content
    )
}

@Composable
fun MagiskDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    destructive: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val contentColor = when {
        !enabled -> MagiskComponentDefaults.PrimaryText.copy(alpha = 0.38f)
        destructive -> MaterialTheme.colorScheme.error
        selected -> MagiskComponentDefaults.PrimaryIconTint
        else -> MagiskComponentDefaults.PrimaryText
    }

    DropdownMenuItem(
        text = {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.62f)
                    )
                }
            }
        },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = if (subtitle != null) 56.dp else 48.dp),
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        } ?: if (selected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null,
        trailingIcon = trailingContent
    )
}

@Composable
fun MagiskItemPicker(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MagiskComponentDefaults.PanelContainer,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MagiskComponentDefaults.PrimaryText
            )
        },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MagiskComponentDefaults.CardContainer,
                border = MagiskComponentDefaults.CardBorder
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(options.size) { index ->
                        val isSelected = index == selectedIndex
                        val contentColor = if (isSelected) {
                            MagiskComponentDefaults.PrimaryIconTint
                        } else {
                            MagiskComponentDefaults.PrimaryText
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        onSelected(index)
                                        onDismiss()
                                    }
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onSelected(index)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MagiskComponentDefaults.PrimaryIconTint,
                                    unselectedColor = MagiskComponentDefaults.SecondaryIconTint
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = options[index],
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                        if (index < options.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MagiskComponentDefaults.DividerColor
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MagiskComponentDefaults.PrimaryIconTint)
            ) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
