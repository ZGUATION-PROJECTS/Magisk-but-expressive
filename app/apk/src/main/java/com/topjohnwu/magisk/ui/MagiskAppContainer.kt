package com.topjohnwu.magisk.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.topjohnwu.magisk.navigation.AppNavigationConfig
import com.topjohnwu.magisk.navigation.AppRoute
import com.topjohnwu.magisk.navigation.AppRouteSpec
import com.topjohnwu.magisk.navigation.rememberMagiskNavController
import com.topjohnwu.magisk.ui.component.MagiskContentColumn
import com.topjohnwu.magisk.ui.component.MagiskEmptyState
import com.topjohnwu.magisk.ui.component.MagiskSnackbarHost
import com.topjohnwu.magisk.ui.component.MagiskTopBar
import com.topjohnwu.magisk.ui.theme.MagiskTheme

@Composable
fun MagiskAppContainer(
    openSection: String? = null,
    navController: NavHostController = rememberMagiskNavController(),
    overlay: @Composable () -> Unit = {}
) {
    MagiskTheme {
        LaunchedEffect(openSection) {
            val route = AppNavigationConfig.routeFromSection(openSection)
            if (route != AppNavigationConfig.startRoute) {
                navController.navigate(AppNavigationConfig.routeString(route)) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        val currentSpec = AppNavigationConfig.routes.firstOrNull { spec ->
            currentDestination?.hierarchy?.any { it.route == spec.graphKey } == true
        } ?: AppNavigationConfig.specFor(AppNavigationConfig.startRoute)

        Scaffold(
            topBar = {
                MagiskTopBar(title = stringResource(currentSpec.labelRes))
            },
            bottomBar = {
                MagiskNavigationBar(
                    currentSpec = currentSpec,
                    navController = navController
                )
            },
            snackbarHost = { MagiskSnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            MagiskNavHost(
                navController = navController,
                innerPadding = innerPadding
            )
        }
        overlay()
    }
}

@Composable
private fun MagiskNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = AppNavigationConfig.startGraphKey,
        modifier = Modifier.padding(innerPadding)
    ) {
        AppNavigationConfig.routes.forEach { spec ->
            composable(spec.graphKey) {
                MagiskRouteSlot(spec = spec)
            }
        }
    }
}

@Composable
private fun MagiskNavigationBar(
    currentSpec: AppRouteSpec,
    navController: NavHostController
) {
    NavigationBar {
        AppNavigationConfig.topLevelRoutes.forEach { spec ->
            val selected = currentSpec.route.id == spec.route.id
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(AppNavigationConfig.routeString(spec.route)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
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
