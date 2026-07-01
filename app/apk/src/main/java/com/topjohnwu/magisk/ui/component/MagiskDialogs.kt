package com.topjohnwu.magisk.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = text?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            if (confirmAction != null) {
                Button(
                    onClick = confirmAction.onClick,
                    enabled = confirmAction.enabled
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            .heightIn(max = 360.dp)
            .padding(vertical = 4.dp),
        shape = MagiskComponentDefaults.ControlShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        content = content
    )
}

@Composable
fun MagiskDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            when {
                selected -> Icon(Icons.Rounded.Check, contentDescription = null)
                leadingIcon != null -> Icon(leadingIcon, contentDescription = null)
            }
        }
    )
}
