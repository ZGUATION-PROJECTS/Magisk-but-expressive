package com.topjohnwu.magisk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import com.topjohnwu.magisk.navigation.AppNavigationConfig
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.navigation.AppRouteSpec
import com.topjohnwu.magisk.navigation.AppNavigator
import com.topjohnwu.magisk.navigation.rememberAppNavigator
import com.topjohnwu.magisk.ui.component.MagiskContentColumn
import com.topjohnwu.magisk.ui.component.MagiskEmptyState
import com.topjohnwu.magisk.ui.component.MagiskSnackbarHost
import com.topjohnwu.magisk.ui.component.MagiskTopBar
import com.topjohnwu.magisk.ui.theme.MagiskTheme

@Composable
fun MagiskAppContainer(
    openSection: String? = null,
    backStack: SnapshotStateList<AppRoute> = remember { mutableStateListOf(AppRoute.Home) },
    overlay: @Composable () -> Unit = {}
) {
    val navigator = rememberAppNavigator(backStack)

    MagiskTheme {
        LaunchedEffect(openSection) {
            val route = AppNavigationConfig.routeFromSection(openSection)
            if (route != AppNavigationConfig.startRoute) {
                navigator.navigateTopLevel(route)
            }
        }

        BackHandler(enabled = backStack.size > 1) {
            navigator.navigateBack()
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val currentRoute = backStack.lastOrNull() ?: AppRoute.Home
        val currentSpec = AppNavigationConfig.specFor(currentRoute)

        Scaffold(
            topBar = {
                MagiskTopBar(title = stringResource(currentSpec.labelRes))
            },
            bottomBar = {
                MagiskNavigationBar(
                    currentSpec = currentSpec,
                    navigator = navigator
                )
            },
            snackbarHost = { MagiskSnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            MagiskNavDisplay(
                backStack = backStack,
                innerPadding = innerPadding
            )
        }
        overlay()
    }
}

@Composable
private fun MagiskNavDisplay(
    backStack: SnapshotStateList<AppRoute>,
    innerPadding: PaddingValues
) {
    val entryProvider = remember {
        entryProvider {
            entry<AppRoute.Home> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Home)) }
            entry<AppRoute.Superuser> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Superuser)) }
            entry<AppRoute.Modules> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Modules)) }
            entry<AppRoute.Logs> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Logs)) }
            entry<AppRoute.Settings> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Settings)) }
            entry<AppRoute.Install> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Install)) }
            entry<AppRoute.DenyList> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.DenyList)) }
            entry<AppRoute.Theme> { MagiskRouteSlot(AppNavigationConfig.specFor(AppRoute.Theme)) }
            entry<AppRoute.Flash> { route -> MagiskRouteSlot(AppNavigationConfig.specFor(route)) }
            entry<AppRoute.ModuleAction> { route -> MagiskRouteSlot(AppNavigationConfig.specFor(route)) }
        }
    }

    Box(modifier = Modifier.padding(innerPadding)) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider
        )
    }
}

@Composable
private fun MagiskNavigationBar(
    currentSpec: AppRouteSpec,
    navigator: AppNavigator
) {
    NavigationBar {
        AppNavigationConfig.topLevelRoutes.forEach { spec ->
            val selected = currentSpec.route.id == spec.route.id
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navigator.navigateTopLevel(spec.route)
                },
                icon = {
                    Icon(
                        imageVector = spec.route.navigationIcon(),
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(spec.labelRes)) }
            )
        }
    }
}

@Composable
private fun MagiskRouteSlot(spec: AppRouteSpec) {
    MagiskContentColumn {
        MagiskEmptyState(
            title = stringResource(spec.labelRes),
            icon = spec.route.navigationIcon()
        )
    }
}

private fun AppRoute.navigationIcon(): ImageVector = when (this) {
    AppRoute.Home -> Icons.Rounded.Home
    AppRoute.Superuser -> Icons.Rounded.Security
    AppRoute.Modules -> Icons.Rounded.Extension
    AppRoute.Logs -> Icons.AutoMirrored.Rounded.Article
    AppRoute.Settings -> Icons.Rounded.Settings
    AppRoute.Install -> Icons.Rounded.SystemUpdate
    AppRoute.DenyList -> Icons.Rounded.Security
    AppRoute.Theme -> Icons.Rounded.Palette
    is AppRoute.Flash,
    is AppRoute.ModuleAction -> Icons.Rounded.Terminal
}
