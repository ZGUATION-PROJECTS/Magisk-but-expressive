package com.topjohnwu.magisk.ui.install

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Article
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
import com.topjohnwu.magisk.arch.UiEffect
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.ui.component.card.MagiskCard
import com.topjohnwu.magisk.viewmodel.install.InstallViewModel
import com.topjohnwu.magisk.view.SystemToastManager
import kotlinx.coroutines.launch
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun InstallScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: InstallViewModel = viewModel(factory = VMFactory)
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Keep state values reactive in Compose
    var keepVerity by remember { mutableStateOf(Config.keepVerity) }
    var keepEnc by remember { mutableStateOf(Config.keepEnc) }

    // File Picker Launcher
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPatchFileSelected(uri)
        }
        viewModel.onFilePickerConsumed()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UiEffect.RequestFilePicker -> {
                    filePicker.launch("*/*")
                }

                is UiEffect.Navigate -> {
                    onNavigate(effect.route)
                }

                is UiEffect.Message -> {
                    val messageString = when (val text = effect.text) {
                        is UiText.Plain -> text.value
                        is UiText.Resource -> context.getString(
                            text.resId, *text.args.toTypedArray()
                        )
                    }
                    SystemToastManager.show(context, messageString)
                }

                else -> {}
            }
        }
    }

    if (state.showSecondSlotWarning) {
        MagiskDialog(
            onDismissRequest = viewModel::onSecondSlotWarningConsumed,
            title = stringResource(CoreR.string.install_inactive_slot),
            text = stringResource(CoreR.string.install_inactive_slot_msg),
            icon = Icons.Rounded.Restore,
            confirmAction = MagiskDialogAction(
                text = stringResource(android.R.string.ok),
                onClick = {
                    viewModel.onSecondSlotWarningConsumed()
                    viewModel.install()
                }
            ),
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = viewModel::onSecondSlotWarningConsumed
            )
        )
    }

    MagiskLazyContent(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp)
    ) {
        if (state.step == 0) {
            // --- STEP 0: OPTIONS ---
            item {
                CategoryHeader(text = stringResource(CoreR.string.install_options_title))
            }

            item {
                MagiskSwitchItem(
                    title = stringResource(CoreR.string.keep_dm_verity),
                    checked = keepVerity,
                    onCheckedChange = { checked ->
                        keepVerity = checked
                        Config.keepVerity = checked
                    },
                    leadingIcon = Icons.Rounded.Security
                )
            }

            item {
                MagiskSwitchItem(
                    title = stringResource(CoreR.string.keep_force_encryption),
                    checked = keepEnc,
                    onCheckedChange = { checked ->
                        keepEnc = checked
                        Config.keepEnc = checked
                    },
                    leadingIcon = Icons.Rounded.Lock
                )
            }

            item {
                Button(
                    onClick = viewModel::nextStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(android.R.string.ok))
                }
            }

        } else {
            // --- STEP 1: METHODS & CHANGELOG ---
            item {
                CategoryHeader(text = stringResource(CoreR.string.install_method_title))
            }

            // Patch File Method
            item {
                val subtitleText = state.patchUri?.let { getFileName(it, context) }
                    ?: stringResource(CoreR.string.select_patch_file_summary)
                MagiskListItem(
                    title = stringResource(CoreR.string.select_patch_file),
                    subtitle = subtitleText,
                    leadingIcon = Icons.AutoMirrored.Rounded.Article,
                    trailingContent = {
                        RadioButton(
                            selected = state.method == InstallViewModel.Method.PATCH,
                            onClick = { viewModel.selectMethod(InstallViewModel.Method.PATCH) })
                    },
                    onClick = { viewModel.selectMethod(InstallViewModel.Method.PATCH) })
            }

            // Direct Install Method (Requires Root)
            if (viewModel.isRooted) {
                item {
                    MagiskListItem(
                        title = stringResource(CoreR.string.direct_install),
                        subtitle = stringResource(CoreR.string.direct_install_summary),
                        leadingIcon = Icons.Rounded.Bolt,
                        trailingContent = {
                            RadioButton(
                                selected = state.method == InstallViewModel.Method.DIRECT,
                                onClick = { viewModel.selectMethod(InstallViewModel.Method.DIRECT) })
                        },
                        onClick = { viewModel.selectMethod(InstallViewModel.Method.DIRECT) })
                }
            }

            // Inactive Slot Install Method
            if (!viewModel.noSecondSlot) {
                item {
                    MagiskListItem(
                        title = stringResource(CoreR.string.install_inactive_slot),
                        subtitle = stringResource(CoreR.string.install_inactive_slot_summary),
                        leadingIcon = Icons.Rounded.Restore,
                        trailingContent = {
                            RadioButton(
                                selected = state.method == InstallViewModel.Method.INACTIVE_SLOT,
                                onClick = { viewModel.selectMethod(InstallViewModel.Method.INACTIVE_SLOT) })
                        },
                        onClick = { viewModel.selectMethod(InstallViewModel.Method.INACTIVE_SLOT) })
                }
            }

            // Changelog Section
            if (state.notes.isNotBlank()) {
                item {
                    CategoryHeader(text = stringResource(CoreR.string.app_changelog))
                }
                item {
                    MagiskCard(modifier = Modifier.fillMaxWidth()) {
                        ChangelogContent(markdown = state.notes)
                    }
                }
            }

        }
    }
}

@Composable
fun InstallTopBarActions(
    state: InstallViewModel.UiState,
    canInstall: Boolean,
    onInstall: () -> Unit
) {
    if (state.step == 1 && state.method != InstallViewModel.Method.NONE) {
        MagiskTopBarIconButton(
            icon = Icons.Rounded.PlayArrow,
            contentDescription = stringResource(CoreR.string.install_start),
            onClick = onInstall,
            enabled = canInstall
        )
    }
}

@Composable
private fun ChangelogContent(
    markdown: String,
    modifier: Modifier = Modifier
) {
    MagiskMarkdown(
        markdown = markdown,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    )
}

@Composable
private fun CategoryHeader(
    text: String, modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    )
}

private fun getFileName(uri: Uri, context: Context): String {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return cursor.getString(index)
                }
            }
        }
    }
    return uri.path?.substringAfterLast('/') ?: uri.toString()
}
