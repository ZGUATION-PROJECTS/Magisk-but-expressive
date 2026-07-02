package com.topjohnwu.magisk.ui.superuser

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.core.model.su.SuPolicy
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.ui.component.card.MagiskActionCard
import com.topjohnwu.magisk.viewmodel.superuser.PolicyUiItem
import com.topjohnwu.magisk.viewmodel.superuser.SuperuserViewModel
import com.topjohnwu.magisk.view.SystemToastManager
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun SuperuserScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SuperuserViewModel = viewModel(factory = SuperuserViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { text ->
            val messageString = when (text) {
                is UiText.Plain -> text.value
                is UiText.Resource -> context.getString(text.resId, *text.args.toTypedArray())
            }
            SystemToastManager.show(context, messageString)
        }
    }

    if (state.loading) {
        MagiskLoadingState(modifier = modifier)
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            SuperuserLogsEntry(
                onClick = { onNavigate(AppRoute.SuperuserLogs) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.items.isEmpty()) {
                MagiskEmptyState(
                    title = stringResource(CoreR.string.superuser_policy_none),
                    icon = Icons.Rounded.Security,
                    modifier = Modifier.weight(1f)
                )
            } else {
                MagiskLazyContent(modifier = Modifier.weight(1f)) {
                    items(
                        count = state.items.size, key = { index ->
                            val item = state.items[index]
                            "${item.uid}:${item.packageName}"
                        }) { index ->
                        val item = state.items[index]
                        MagiskExpandableListItem(
                            title = item.title,
                            subtitle = item.packageName,
                            expanded = item.expanded,
                            onClick = { viewModel.toggleExpanded(item.uid, item.packageName) },
                            leadingContent = {
                                AppIcon(
                                    packageName = item.iconPackageName ?: item.packageName,
                                    modifier = Modifier.size(40.dp)
                                )
                            },
                            headerTrailingContent = {
                                PolicyStatusBadge(policy = item.policy)
                            }) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                // 1. Policy selection
                                val options = remember(item.showSlider) {
                                    val list = mutableListOf(
                                        MagiskChipOption(
                                            SuPolicy.DENY, context.getString(CoreR.string.deny)
                                        ), MagiskChipOption(
                                            SuPolicy.ALLOW, context.getString(CoreR.string.grant)
                                        )
                                    )
                                    if (item.showSlider) {
                                        list.add(
                                            1, MagiskChipOption(
                                                SuPolicy.RESTRICT, context.getString(CoreR.string.restrict)
                                            )
                                        )
                                    }
                                    list
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(CoreR.string.superuser_setting),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    MagiskFilterChipRow(
                                        options = options,
                                        selected = item.policy,
                                        onSelected = { newPolicy ->
                                            viewModel.setPolicy(
                                                item.uid, newPolicy
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 2. Notification Toggle
                                MagiskSwitchItem(
                                    title = stringResource(CoreR.string.superuser_toggle_notification),
                                    checked = item.notification,
                                    onCheckedChange = { viewModel.toggleNotify(item) },
                                    leadingIcon = Icons.Rounded.Notifications
                                )

                                // 3. Logging Toggle
                                MagiskSwitchItem(
                                    title = stringResource(CoreR.string.superuser_toggle_logging),
                                    checked = item.logging,
                                    onCheckedChange = { viewModel.toggleLog(item) },
                                    leadingIcon = Icons.Rounded.History
                                )

                                // 4. Revoke Button
                                Button(
                                    onClick = { viewModel.revoke(item) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(
                                        Icons.Rounded.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = stringResource(CoreR.string.superuser_toggle_revoke))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuperuserLogsEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MagiskActionCard(
        onClick = onClick,
        modifier = modifier,
        minHeight = 64.dp
    ) {
        Icon(
            imageVector = Icons.Rounded.History,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(CoreR.string.superuser_logs),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(CoreR.string.logs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(22.dp)
        )
    }
}
