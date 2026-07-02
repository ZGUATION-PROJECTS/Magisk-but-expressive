package com.topjohnwu.magisk.ui.module

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.ui.component.card.MagiskWarningCard
import com.topjohnwu.magisk.viewmodel.module.ModuleUiItem
import com.topjohnwu.magisk.viewmodel.module.ModuleViewModel
import com.topjohnwu.magisk.view.SystemToastManager
import com.topjohnwu.magisk.core.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: ModuleViewModel = viewModel(factory = ModuleViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val fabVisible by remember {
        derivedStateOf { 
            state.filteredModules.isEmpty() || 
            listState.firstVisibleItemIndex > 0 || 
            listState.firstVisibleItemScrollOffset > 2 
        }
    }

    // Dialog state for update confirmation
    var pendingUpdateModule by remember { mutableStateOf<ModuleUiItem?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onNavigate(
                AppRoute.Flash(
                    action = Const.Value.FLASH_ZIP, additionalData = uri.toString()
                )
            )
        }
    }

    LaunchedEffect(viewModel) {
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

    // Update confirmation dialog
    pendingUpdateModule?.let { item ->
        val updateModule = item.update ?: return@let
        MagiskDialog(
            title = stringResource(CoreR.string.confirm_install_title),
            text = stringResource(CoreR.string.confirm_install, updateModule.name),
            icon = Icons.Rounded.SystemUpdate,
            onDismissRequest = { pendingUpdateModule = null },
            confirmAction = MagiskDialogAction(
                text = stringResource(android.R.string.ok), onClick = {
                    pendingUpdateModule = null
                    onNavigate(
                        AppRoute.Flash(
                            action = Const.Value.FLASH_ZIP, additionalData = updateModule.zipUrl
                        )
                    )
                }),
            dismissAction = MagiskDialogAction(
                text = stringResource(android.R.string.cancel),
                onClick = { pendingUpdateModule = null }))
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (state.loading) {
            MagiskLoadingState(modifier = Modifier.fillMaxSize())
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(visible = state.searchVisible) {
                    MagiskSearchField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = stringResource(CoreR.string.hide_search),
                        clearContentDescription = stringResource(CoreR.string.clear_search),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (state.filteredModules.isEmpty()) {
                    MagiskEmptyState(
                        title = stringResource(CoreR.string.module_empty),
                        icon = Icons.Rounded.ExtensionOff,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MagiskLazyContent(
                        modifier = Modifier.weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(
                            top = 8.dp, bottom = 120.dp, start = 16.dp, end = 16.dp
                        )
                    ) {
                        items(
                            count = state.filteredModules.size,
                            key = { index -> state.filteredModules[index].id }) { index ->
                            val item = state.filteredModules[index]
                            MagiskExpandableListItem(
                                title = item.name,
                                subtitle = item.versionAuthor,
                                expanded = item.expanded,
                                onClick = { viewModel.toggleExpanded(item.id) },
                                leadingIcon = Icons.Rounded.Extension,
                                headerTrailingContent = {
                                    if (item.removed) {
                                        Text(
                                            text = stringResource(CoreR.string.module_state_remove),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        Switch(
                                            checked = item.enabled,
                                            onCheckedChange = { viewModel.toggleEnabled(item.id) })
                                    }
                                }) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                    Text(
                                        text = item.description.ifBlank { stringResource(CoreR.string.no_info_provided) },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Display notice text (if present)
                                    item.noticeText?.let { notice ->
                                        val noticeStr = when (notice) {
                                            is UiText.Plain -> notice.value
                                            is UiText.Resource -> stringResource(
                                                notice.resId, *notice.args.toTypedArray()
                                            )
                                        }
                                        MagiskWarningCard(message = noticeStr)
                                    }

                                    // Badges SUGGESTION row
                                    if (item.badges.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            item.badges.forEach { badge ->
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text(text = badge) })
                                            }
                                        }
                                    }

                                    // Actions row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (item.showAction) {
                                            Button(
                                                onClick = {
                                                    onNavigate(
                                                        AppRoute.ModuleAction(
                                                            item.id, item.name
                                                        )
                                                    )
                                                }, modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.PlayArrow,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = stringResource(CoreR.string.module_action))
                                            }
                                        }

                                        if (item.updateReady && item.update != null) {
                                            Button(
                                                onClick = { pendingUpdateModule = item },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Update, contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = stringResource(CoreR.string.module_badge_update))
                                            }
                                        }

                                        if (item.removed) {
                                            Button(
                                                onClick = { viewModel.toggleRemove(item.id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Restore, contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = stringResource(CoreR.string.module_state_restore))
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewModel.toggleRemove(item.id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                border = BorderStroke(
                                                    1.dp, MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Rounded.Delete, contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = stringResource(CoreR.string.module_state_remove))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = fabVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { filePickerLauncher.launch("application/zip") },
                icon = { Icon(Icons.Rounded.FolderOpen, contentDescription = null) },
                text = { Text(text = stringResource(CoreR.string.module_action_install_external)) }
            )
        }
    }
}

@Composable
fun ModulesTopBarActions(
    searchVisible: Boolean,
    onToggleSearch: () -> Unit
) {
    MagiskTopBarIconButton(
        icon = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
        contentDescription = stringResource(CoreR.string.hide_search),
        onClick = onToggleSearch
    )
}
