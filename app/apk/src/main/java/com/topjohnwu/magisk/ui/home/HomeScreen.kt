package com.topjohnwu.magisk.ui.home

import android.R
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.topjohnwu.magisk.arch.UiEffect
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.ui.component.card.*
import com.topjohnwu.magisk.viewmodel.home.HomeViewModel
import com.topjohnwu.magisk.view.SystemToastManager
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showUninstallDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { text ->
            val messageString = when (text) {
                is UiText.Plain -> text.value
                is UiText.Resource -> context.getString(text.resId, *text.args.toTypedArray())
            }
            SystemToastManager.show(context, messageString)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UiEffect.OpenUri -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, effect.uri))
                }

                is UiEffect.Reboot -> {
                    com.topjohnwu.magisk.core.ktx.reboot()
                }

                is UiEffect.Navigate -> {
                    onNavigate(effect.route)
                }

                else -> {}
            }
        }
    }

    // --- DIALOGS ---

    if (state.envFixCode != 0) {
        val isFullFix = state.envFixCode == 1
        MagiskDialog(
            title = stringResource(CoreR.string.env_fix_title),
            onDismissRequest = viewModel::onEnvFixConsumed,
            text = stringResource(if (isFullFix) CoreR.string.env_full_fix_msg else CoreR.string.env_fix_msg),
            confirmAction = if (!isFullFix) {
                MagiskDialogAction(
                    text = stringResource(android.R.string.ok), onClick = {
                        viewModel.onEnvFixConsumed()
                        com.topjohnwu.magisk.core.ktx.reboot()
                    })
            } else {
                MagiskDialogAction(
                    text = stringResource(CoreR.string.install), onClick = {
                        viewModel.onEnvFixConsumed()
                        onNavigate(AppRoute.Install)
                    })
            },
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = viewModel::onEnvFixConsumed
            ))
    }

    if (state.showHideRestore) {
        MagiskDialog(
            title = stringResource(CoreR.string.restore),
            onDismissRequest = viewModel::onHideRestoreConsumed,
            text = stringResource(CoreR.string.restore_img_msg),
            confirmAction = MagiskDialogAction(
                text = stringResource(android.R.string.ok), onClick = {
                    viewModel.restoreImages()
                    viewModel.onHideRestoreConsumed()
                }),
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = viewModel::onHideRestoreConsumed
            ))
    }

    if (state.showManagerInstall) {
        MagiskDialog(
            title = stringResource(CoreR.string.update),
            onDismissRequest = viewModel::onManagerInstallConsumed,
            text = state.managerReleaseNotes.ifEmpty { stringResource(CoreR.string.update_available) },
            confirmAction = MagiskDialogAction(
                text = stringResource(android.R.string.ok), onClick = {
                    viewModel.onManagerInstallConsumed()
                    viewModel.openLink(Info.update.link)
                }),
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = viewModel::onManagerInstallConsumed
            ))
    }

    if (showUninstallDialog) {
        MagiskDialog(
            title = stringResource(CoreR.string.uninstall_magisk_title),
            onDismissRequest = { showUninstallDialog = false },
            icon = Icons.Rounded.DeleteSweep,
            textContent = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(CoreR.string.uninstall_magisk_msg),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    MagiskCard(onClick = {
                        showUninstallDialog = false
                        viewModel.restoreImages()
                    }) {
                        MagiskListItem(
                            title = stringResource(CoreR.string.restore_img),
                            subtitle = stringResource(CoreR.string.uninstall_restore_images_subtitle),
                            leadingIcon = Icons.Rounded.SettingsBackupRestore
                        )
                    }
                    MagiskCard(onClick = {
                        showUninstallDialog = false
                        onNavigate(AppRoute.Flash(Const.Value.UNINSTALL))
                    }) {
                        MagiskListItem(
                            title = stringResource(CoreR.string.complete_uninstall),
                            subtitle = stringResource(CoreR.string.uninstall_complete_subtitle),
                            leadingIcon = Icons.Rounded.DeleteForever
                        )
                    }
                }
            },
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = { showUninstallDialog = false }
            )
        )
    }

    // --- MAIN SCREEN LAYOUT ---

    MagiskLazyContent(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = MagiskComponentDefaults.ScreenHorizontalPadding,
            top = 12.dp,
            end = MagiskComponentDefaults.ScreenHorizontalPadding,
            bottom = 132.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Safety Notice
        if (state.noticeVisible) {
            item {
                MagiskWarningCard(
                    title = stringResource(CoreR.string.home_safety_warning),
                    message = stringResource(CoreR.string.home_notice_content),
                    dismissContentDescription = stringResource(R.string.cancel),
                    onDismiss = viewModel::hideNotice
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 2. Magisk Status Card
        item {
            val isInstalled = state.magiskState != HomeViewModel.State.INVALID
            MagiskStatusCard(
                title = "Magisk",
                statusText = if (isInstalled) {
                    stringResource(CoreR.string.home_installed_version)
                } else {
                    stringResource(CoreR.string.not_installed)
                },
                statusColor = if (isInstalled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                icon = Icons.Rounded.Security,
                iconContainerColor = MaterialTheme.colorScheme.primary,
                iconTint = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 32.dp),
                metrics = listOf(
                    MagiskStatusMetric(
                        label = stringResource(CoreR.string.home_installed_version),
                        value = if (isInstalled) state.magiskInstalledVersion else stringResource(
                            CoreR.string.not_available
                        )
                    ),
                    MagiskStatusMetric(
                        label = stringResource(CoreR.string.home_latest_version),
                        value = state.managerRemoteVersion.ifEmpty {
                            stringResource(CoreR.string.not_available)
                        }
                    )
                ),
                primaryAction = MagiskCardAction(
                    text = if (isInstalled) {
                        stringResource(CoreR.string.reinstall)
                    } else {
                        stringResource(CoreR.string.install)
                    },
                    onClick = { onNavigate(AppRoute.Install) },
                    icon = Icons.Rounded.Download
                ),
                secondaryAction = if (isInstalled) {
                    MagiskCardAction(
                        text = stringResource(CoreR.string.uninstall),
                        onClick = { showUninstallDialog = true },
                        icon = Icons.Rounded.DeleteForever,
                        style = MagiskCardActionStyle.Destructive
                    )
                } else {
                    null
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 3. Magisk App Status Card
        item {
            MagiskStatusCard(
                title = stringResource(CoreR.string.home_app_title),
                statusText = stringResource(CoreR.string.home_package) + ": " + state.packageName,
                statusColor = MaterialTheme.colorScheme.secondary,
                icon = Icons.Rounded.Android,
                iconContainerColor = MaterialTheme.colorScheme.secondary,
                iconTint = MaterialTheme.colorScheme.onSecondary,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 32.dp, bottomStart = 32.dp, bottomEnd = 4.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                metrics = listOf(
                    MagiskStatusMetric(
                        label = stringResource(CoreR.string.home_installed_version),
                        value = state.managerInstalledVersion
                    ),
                    MagiskStatusMetric(
                        label = stringResource(CoreR.string.home_latest_version),
                        value = state.managerRemoteVersion.ifEmpty {
                            stringResource(CoreR.string.not_available)
                        }
                    )
                ),
                primaryAction = MagiskCardAction(
                    text = stringResource(CoreR.string.update),
                    onClick = { onNavigate(AppRoute.AppUpdate) },
                    icon = Icons.Rounded.Update
                )
            )
        }

        // 4. Support Us Panel
        item {
            MagiskSupportCard(
                title = stringResource(CoreR.string.home_support_title),
                message = stringResource(CoreR.string.home_support_content),
                primaryAction = MagiskCardAction(
                    text = stringResource(CoreR.string.donate),
                    onClick = { viewModel.openLink("https://github.com/sponsors/topjohnwu") },
                    icon = Icons.Rounded.Favorite
                ),
                secondaryAction = MagiskCardAction(
                    text = stringResource(CoreR.string.documents),
                    onClick = { viewModel.openLink("https://topjohnwu.github.io/Magisk/") },
                    style = MagiskCardActionStyle.Secondary
                )
            )
        }

        // 5. Contributors Section
        item {
            MagiskSection(
                title = stringResource(CoreR.string.home_section_contributors),
                icon = Icons.Rounded.People
            ) {
                if (state.contributorsLoading) {
                    MagiskLoadingState()
                } else {
                    state.contributors.chunked(2).forEach { rowContributors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowContributors.forEachIndexed { index, contributor ->
                                val isEven = index % 2 == 0
                                val cardShape = if (isEven) {
                                    RoundedCornerShape(topStart = 28.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 28.dp)
                                } else {
                                    RoundedCornerShape(topStart = 8.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 8.dp)
                                }
                                val avatarShape = if (isEven) {
                                    RoundedCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 12.dp)
                                } else {
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 4.dp)
                                }

                                MagiskCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 160.dp),
                                    shape = cardShape,
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AsyncImage(
                                            model = contributor.avatarUrl,
                                            contentDescription = contributor.login,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(avatarShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        Text(
                                            text = contributor.login,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MagiskComponentDefaults.PrimaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            contributor.links.forEach { link ->
                                                IconButton(
                                                    onClick = { viewModel.openLink(link.url) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        painter = painterResource(link.iconRes),
                                                        contentDescription = stringResource(link.labelRes),
                                                        modifier = Modifier.size(18.dp),
                                                        tint = MagiskComponentDefaults.PrimaryIconTint
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowContributors.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBarActions(
    viewModel: HomeViewModel
) {
    val state by viewModel.state.collectAsState()

    if (!state.runtime.isInstalled) return

    var expanded by remember { mutableStateOf(false) }

    Box {
        MagiskTopBarIconButton(
            icon = Icons.Rounded.RestartAlt,
            contentDescription = stringResource(CoreR.string.reboot),
            onClick = { expanded = true }
        )
        MagiskDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot),
                subtitle = "Riavvio completo del sistema",
                leadingIcon = Icons.Rounded.RestartAlt,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot()
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot_userspace),
                subtitle = "Riavvio veloce dei servizi app",
                leadingIcon = Icons.Rounded.Refresh,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot("userspace")
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot_recovery),
                subtitle = "Accedi alla modalità di ripristino",
                leadingIcon = Icons.Rounded.SettingsBackupRestore,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot("recovery")
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot_bootloader),
                subtitle = "Accedi alla modalità Fastboot",
                leadingIcon = Icons.Rounded.SettingsInputHdmi,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot("bootloader")
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot_download),
                subtitle = "Accedi alla modalità Download (Samsung)",
                leadingIcon = Icons.Rounded.Download,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot("download")
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.reboot_edl),
                subtitle = "Emergency Download Mode (Qualcomm)",
                leadingIcon = Icons.Rounded.DeveloperMode,
                onClick = {
                    expanded = false
                    com.topjohnwu.magisk.core.ktx.reboot("edl")
                }
            )
        }
    }
}
