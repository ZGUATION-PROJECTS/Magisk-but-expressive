package com.topjohnwu.magisk.ui.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.ui.component.*
import com.topjohnwu.magisk.ui.component.card.MagiskCard
import com.topjohnwu.magisk.viewmodel.log.LogDisplayFilter
import com.topjohnwu.magisk.viewmodel.log.MagiskLogLevel
import com.topjohnwu.magisk.viewmodel.log.MagiskLogScreenUiState
import com.topjohnwu.magisk.viewmodel.log.MagiskLogUiItem
import com.topjohnwu.magisk.viewmodel.log.MagiskLogViewModel
import com.topjohnwu.magisk.view.SystemToastManager
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun LogsScreen(
    onNavigate: (AppRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: MagiskLogViewModel = viewModel(factory = MagiskLogViewModel.Factory)
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val logListState = rememberLazyListState()
    var controlsVisible by remember { mutableStateOf(true) }

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

    LaunchedEffect(logListState) {
        snapshotFlow {
            Triple(
                logListState.firstVisibleItemIndex,
                logListState.firstVisibleItemScrollOffset,
                logListState.lastScrolledBackward
            )
        }.collect { (index, offset, scrollingBack) ->
            controlsVisible = scrollingBack || (index == 0 && offset < 24)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (state.loading) {
            MagiskLoadingState(modifier = Modifier.fillMaxSize())
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = controlsVisible && state.searchVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    MagiskSearchField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = stringResource(CoreR.string.log_search_hint),
                        clearContentDescription = stringResource(CoreR.string.clear_search),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                // Log List
                if (state.filteredLogs.isEmpty()) {
                    MagiskEmptyState(
                        title = stringResource(CoreR.string.log_filtered_empty_title),
                        icon = Icons.Rounded.FilterNone,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    MagiskLazyContent(
                        modifier = Modifier.weight(1f),
                        state = logListState,
                        contentPadding = PaddingValues(
                            top = 4.dp, bottom = 96.dp, start = 16.dp, end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            count = state.filteredLogs.size,
                            key = { index -> state.filteredLogs[index].id }) { index ->
                            LogItem(item = state.filteredLogs[index])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogsTopBarActions(
    searchVisible: Boolean,
    selectedFilter: LogDisplayFilter,
    onToggleSearch: () -> Unit,
    onFilterSelected: (LogDisplayFilter) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    MagiskTopBarIconButton(
        icon = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
        contentDescription = stringResource(CoreR.string.hide_search),
        onClick = onToggleSearch
    )

    Box {
        MagiskTopBarIconButton(
            icon = if (selectedFilter == LogDisplayFilter.ALL) {
                Icons.Rounded.MoreVert
            } else {
                Icons.Rounded.FilterList
            },
            contentDescription = stringResource(CoreR.string.denylist_search_filters),
            onClick = { expanded = true }
        )
        MagiskDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.save_log),
                subtitle = "Esporta il file di log sulla memoria",
                leadingIcon = Icons.Rounded.Save,
                onClick = {
                    onSave()
                    expanded = false
                }
            )
            MagiskDropdownMenuItem(
                text = stringResource(CoreR.string.clear_log),
                subtitle = "Svuota la cronologia attuale",
                destructive = true,
                leadingIcon = Icons.Rounded.DeleteSweep,
                onClick = {
                    onClear()
                    expanded = false
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            LogDisplayFilter.entries.forEach { filter ->
                MagiskDropdownMenuItem(
                    text = stringResource(filter.labelRes),
                    selected = filter == selectedFilter,
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}
