package com.topjohnwu.magisk.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AddToHomeScreen
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.arch.UIActivity
import com.topjohnwu.magisk.arch.UiEffect
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.tasks.AppMigration
import com.topjohnwu.magisk.core.utils.LocaleSetting
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.viewmodel.settings.SettingsViewModel
import com.topjohnwu.magisk.viewmodel.settings.SU_TIMEOUT_VALUES
import com.topjohnwu.magisk.view.SystemToastManager
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

private enum class PickerType {
    LANGUAGE, UPDATE_CHANNEL, SU_ACCESS, MULTIUSER, NAMESPACE, AUTO_RESPONSE, TIMEOUT, NOTIFICATION
}

@Composable
fun SettingsScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Dialog & Picker States
    var activePicker by remember { mutableStateOf<PickerType?>(null) }
    var showCustomChannelUrlDialog by remember { mutableStateOf(false) }
    var showHideAppDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.refreshState()
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
            val uiActivity = context as? UIActivity<*> ?: return@collect
            when (effect) {
                is UiEffect.RequestHideApp -> {
                    uiActivity.withPermission("android.permission.REQUEST_INSTALL_PACKAGES") { granted ->
                        if (granted) {
                            coroutineScope.launch {
                                val success = AppMigration.patchAndHide(context, effect.label)
                                viewModel.onAppMigrationResult(success)
                            }
                        } else {
                            viewModel.onAppMigrationResult(false)
                        }
                    }
                }

                UiEffect.RequestRestoreApp -> {
                    uiActivity.withPermission("android.permission.REQUEST_INSTALL_PACKAGES") { granted ->
                        if (granted) {
                            coroutineScope.launch {
                                val success = AppMigration.restoreApp(context)
                                viewModel.onAppMigrationResult(success)
                            }
                        } else {
                            viewModel.onAppMigrationResult(false)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // Custom Channel URL Dialog
    if (showCustomChannelUrlDialog) {
        var tempUrl by remember { mutableStateOf(state.customChannelUrl) }
        AlertDialog(title = { Text(stringResource(CoreR.string.settings_update_custom)) }, text = {
            OutlinedTextField(
                value = tempUrl,
                onValueChange = { tempUrl = it },
                label = { Text(stringResource(CoreR.string.settings_update_custom_msg)) },
                modifier = Modifier.fillMaxWidth()
            )
        }, onDismissRequest = { showCustomChannelUrlDialog = false }, confirmButton = {
            TextButton(
                onClick = {
                    showCustomChannelUrlDialog = false
                    viewModel.setCustomChannelUrl(tempUrl)
                }) {
                Text(stringResource(android.R.string.ok))
            }
        }, dismissButton = {
            TextButton(onClick = { showCustomChannelUrlDialog = false }) {
                Text(stringResource(android.R.string.cancel))
            }
        })
    }

    // Hide App Dialog
    if (showHideAppDialog) {
        val defaultLabel = stringResource(CoreR.string.settings)
        var tempLabel by remember(defaultLabel) { mutableStateOf(defaultLabel) }
        AlertDialog(title = { Text(stringResource(CoreR.string.settings_hide_app_title)) }, text = {
            OutlinedTextField(
                value = tempLabel,
                onValueChange = { tempLabel = it },
                label = { Text(stringResource(CoreR.string.settings_app_name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
        }, onDismissRequest = { showHideAppDialog = false }, confirmButton = {
            TextButton(
                onClick = {
                    showHideAppDialog = false
                    viewModel.requestHideApp(tempLabel)
                }) {
                Text(stringResource(android.R.string.ok))
            }
        }, dismissButton = {
            TextButton(onClick = { showHideAppDialog = false }) {
                Text(stringResource(android.R.string.cancel))
            }
        })
    }

    // Picker Dialog picker
    activePicker?.let { picker ->
        val dialogTitle = when (picker) {
            PickerType.LANGUAGE -> stringResource(CoreR.string.language)
            PickerType.UPDATE_CHANNEL -> stringResource(CoreR.string.settings_update_channel_title)
            PickerType.SU_ACCESS -> stringResource(CoreR.string.superuser_access)
            PickerType.MULTIUSER -> stringResource(CoreR.string.multiuser_mode)
            PickerType.NAMESPACE -> stringResource(CoreR.string.mount_namespace_mode)
            PickerType.AUTO_RESPONSE -> stringResource(CoreR.string.auto_response)
            PickerType.TIMEOUT -> stringResource(CoreR.string.request_timeout)
            PickerType.NOTIFICATION -> stringResource(CoreR.string.superuser_notification)
        }

        val options: List<String> = when (picker) {
            PickerType.LANGUAGE -> LocaleSetting.available.names.toList()
            PickerType.UPDATE_CHANNEL -> context.resources.getStringArray(CoreR.array.update_channel)
                .toList()

            PickerType.SU_ACCESS -> context.resources.getStringArray(CoreR.array.su_access).toList()
            PickerType.MULTIUSER -> context.resources.getStringArray(CoreR.array.multiuser_mode)
                .toList()

            PickerType.NAMESPACE -> context.resources.getStringArray(CoreR.array.namespace).toList()
            PickerType.AUTO_RESPONSE -> context.resources.getStringArray(CoreR.array.auto_response)
                .toList()

            PickerType.TIMEOUT -> context.resources.getStringArray(CoreR.array.request_timeout)
                .toList()

            PickerType.NOTIFICATION -> context.resources.getStringArray(CoreR.array.su_notification)
                .toList()
        }

        val selectedIndex = when (picker) {
            PickerType.LANGUAGE -> state.languageIndex
            PickerType.UPDATE_CHANNEL -> state.updateChannel
            PickerType.SU_ACCESS -> state.rootMode
            PickerType.MULTIUSER -> state.suMultiuserMode
            PickerType.NAMESPACE -> state.suMntNamespaceMode
            PickerType.AUTO_RESPONSE -> state.suAutoResponse
            PickerType.TIMEOUT -> state.suTimeoutIndex
            PickerType.NOTIFICATION -> state.suNotification
        }

        MagiskItemPicker(
            title = dialogTitle,
            options = options,
            selectedIndex = selectedIndex,
            onSelected = { index ->
                when (picker) {
                    PickerType.LANGUAGE -> viewModel.setLanguageByIndex(index)
                    PickerType.UPDATE_CHANNEL -> viewModel.setUpdateChannel(index)
                    PickerType.SU_ACCESS -> viewModel.setRootMode(index)
                    PickerType.MULTIUSER -> viewModel.setSuMultiuserMode(index)
                    PickerType.NAMESPACE -> viewModel.setSuMntNamespaceMode(index)
                    PickerType.AUTO_RESPONSE -> viewModel.setSuAutoResponse(index)
                    PickerType.TIMEOUT -> viewModel.setSuTimeoutIndex(index)
                    PickerType.NOTIFICATION -> viewModel.setSuNotification(index)
                }
            },
            onDismiss = { activePicker = null })
    }

    MagiskLazyContent(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            MagiskSettingsGroup(
                title = stringResource(CoreR.string.home_app_title),
                icon = Icons.Rounded.Android,
                items = buildList<MagiskSettingsItemContent> {
                    add {
                        val modeLabel = when (state.darkThemeMode) {
                            -1 -> stringResource(CoreR.string.settings_dark_mode_system)
                            0 -> stringResource(CoreR.string.settings_dark_mode_light)
                            1 -> stringResource(CoreR.string.settings_dark_mode_dark)
                            -2 -> stringResource(CoreR.string.settings_dark_mode_amoled)
                            else -> stringResource(CoreR.string.settings_dark_mode_system)
                        }
                        val colorLabel = stringResource(state.themeNameRes)
                        MagiskSettingsListItem(
                            title = stringResource(CoreR.string.theme_appearance),
                            subtitle = "$modeLabel / $colorLabel",
                            leadingIcon = Icons.Rounded.Palette,
                            onClick = { onNavigate(AppRoute.Theme) })
                    }
                    add {
                        MagiskSettingsListItem(
                            title = stringResource(CoreR.string.language),
                            subtitle = state.languageName,
                            leadingIcon = Icons.Rounded.Language,
                            onClick = { onNavigate(AppRoute.Language) })
                    }
                    if (state.canAddShortcut) {
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.add_shortcut_title),
                                subtitle = stringResource(CoreR.string.setting_add_shortcut_summary),
                                leadingIcon = Icons.AutoMirrored.Rounded.AddToHomeScreen,
                                onClick = viewModel::addShortcut
                            )
                        }
                    }
                    if (state.isHiddenApp) {
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.settings_restore_app_title),
                                subtitle = stringResource(CoreR.string.settings_restore_app_summary),
                                leadingIcon = Icons.Rounded.SettingsBackupRestore,
                                onClick = viewModel::requestRestoreApp
                            )
                        }
                    } else if (state.canMigrateApp) {
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.settings_hide_app_title),
                                subtitle = stringResource(CoreR.string.settings_hide_app_summary),
                                leadingIcon = Icons.Rounded.HideSource,
                                onClick = { showHideAppDialog = true })
                        }
                    }
                }
            )
        }

        item {
            MagiskSettingsGroup(
                title = "Magisk",
                icon = Icons.Rounded.Security,
                items = buildList<MagiskSettingsItemContent> {
                    add {
                        MagiskSettingsSwitchItem(
                            title = stringResource(CoreR.string.settings_check_update_title),
                            checked = state.checkUpdate,
                            onCheckedChange = viewModel::setCheckUpdate,
                            leadingIcon = Icons.Rounded.Update
                        )
                    }
                    add {
                        MagiskSettingsListItem(
                            title = stringResource(CoreR.string.settings_update_channel_title),
                            subtitle = state.updateChannelName,
                            leadingIcon = Icons.AutoMirrored.Rounded.CallSplit,
                            onClick = { activePicker = PickerType.UPDATE_CHANNEL })
                    }
                    if (state.isCustomChannel) {
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.settings_update_custom),
                                subtitle = state.customChannelUrl.ifBlank { stringResource(CoreR.string.not_available) },
                                leadingIcon = Icons.Rounded.Link,
                                onClick = { showCustomChannelUrlDialog = true })
                        }
                    }
                    add {
                        MagiskSettingsSwitchItem(
                            title = stringResource(CoreR.string.settings_doh_title),
                            checked = state.doh,
                            onCheckedChange = viewModel::setDoH,
                            leadingIcon = Icons.Rounded.Lock
                        )
                    }
                    if (state.showMagisk) {
                        add {
                            MagiskSettingsSwitchItem(
                                title = "Zygisk",
                                checked = state.zygisk,
                                onCheckedChange = viewModel::setZygisk,
                                leadingIcon = Icons.Rounded.Bolt,
                                subtitle = stringResource(CoreR.string.settings_zygisk_summary)
                            )
                        }
                        add {
                            MagiskSettingsSwitchItem(
                                title = stringResource(CoreR.string.settings_denylist_title),
                                checked = state.denyList,
                                onCheckedChange = viewModel::setDenyList,
                                leadingIcon = Icons.Rounded.Block,
                                subtitle = stringResource(CoreR.string.settings_denylist_summary)
                            )
                        }
                        if (state.showDenyListConfig) {
                            add {
                                MagiskSettingsListItem(
                                    title = stringResource(CoreR.string.settings_denylist_config_title),
                                    subtitle = stringResource(CoreR.string.settings_denylist_config_summary),
                                    leadingIcon = Icons.AutoMirrored.Rounded.Rule,
                                    onClick = { onNavigate(AppRoute.DenyList) })
                            }
                        }
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.settings_hosts_title),
                                subtitle = stringResource(CoreR.string.settings_hosts_summary),
                                leadingIcon = Icons.Rounded.Dns,
                                onClick = viewModel::createSystemlessHosts
                            )
                        }
                    }
                }
            )
        }

        if (state.showSuperuser) {
            item {
                MagiskSettingsGroup(
                    title = stringResource(CoreR.string.superuser),
                    icon = Icons.Rounded.SupervisorAccount,
                    items = buildList<MagiskSettingsItemContent> {
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.superuser_access),
                                subtitle = state.accessModeName,
                                leadingIcon = Icons.Rounded.SupervisorAccount,
                                onClick = { activePicker = PickerType.SU_ACCESS })
                        }
                        if (state.multiuserModeEnabled) {
                            add {
                                MagiskSettingsListItem(
                                    title = stringResource(CoreR.string.multiuser_mode),
                                    subtitle = state.multiuserModeName,
                                    leadingIcon = Icons.Rounded.People,
                                    onClick = { activePicker = PickerType.MULTIUSER })
                            }
                        }
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.mount_namespace_mode),
                                subtitle = state.mountNamespaceName,
                                leadingIcon = Icons.Rounded.AccountTree,
                                onClick = { activePicker = PickerType.NAMESPACE })
                        }
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.auto_response),
                                subtitle = state.autoResponseName,
                                leadingIcon = Icons.AutoMirrored.Rounded.Reply,
                                onClick = { activePicker = PickerType.AUTO_RESPONSE })
                        }
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.request_timeout),
                                subtitle = state.requestTimeoutName,
                                leadingIcon = Icons.Rounded.HourglassEmpty,
                                onClick = { activePicker = PickerType.TIMEOUT })
                        }
                        add {
                            MagiskSettingsListItem(
                                title = stringResource(CoreR.string.superuser_notification),
                                subtitle = state.suNotificationName,
                                leadingIcon = Icons.Rounded.Notifications,
                                onClick = { activePicker = PickerType.NOTIFICATION })
                        }
                        add {
                            MagiskSettingsSwitchItem(
                                title = stringResource(CoreR.string.settings_su_reauth_title),
                                subtitle = stringResource(CoreR.string.settings_su_reauth_summary),
                                checked = state.suReAuth,
                                onCheckedChange = viewModel::setSuReAuth,
                                leadingIcon = Icons.Rounded.Refresh
                            )
                        }
                        if (state.deviceSecure) {
                            add {
                                MagiskSettingsSwitchItem(
                                    title = stringResource(CoreR.string.settings_su_auth_title),
                                    subtitle = stringResource(CoreR.string.settings_su_auth_summary),
                                    checked = state.suAuth,
                                    onCheckedChange = viewModel::setSuAuth,
                                    leadingIcon = Icons.Rounded.Fingerprint
                                )
                            }
                        }
                        if (!state.hideTapjackOnSPlus) {
                            add {
                                MagiskSettingsSwitchItem(
                                    title = stringResource(CoreR.string.settings_su_tapjack_title),
                                    subtitle = stringResource(CoreR.string.settings_su_tapjack_summary),
                                    checked = state.suTapjack,
                                    onCheckedChange = viewModel::setSuTapjack,
                                    leadingIcon = Icons.Rounded.Security
                                )
                            }
                        }
                        if (state.showRestrict) {
                            add {
                                MagiskSettingsSwitchItem(
                                    title = stringResource(CoreR.string.settings_su_restrict_title),
                                    subtitle = stringResource(CoreR.string.settings_su_restrict_summary),
                                    checked = state.suRestrict,
                                    onCheckedChange = viewModel::setSuRestrict,
                                    leadingIcon = Icons.Rounded.AdminPanelSettings
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

