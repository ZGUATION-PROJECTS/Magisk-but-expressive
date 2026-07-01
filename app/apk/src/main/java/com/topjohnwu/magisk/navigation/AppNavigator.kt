package com.topjohnwu.magisk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController

class AppNavigator internal constructor(
    private val navController: NavHostController
) {

    fun navigate(route: AppRoute) {
        navController.navigate(AppNavigationConfig.routeString(route)) {
            launchSingleTop = true
        }
    }

    fun navigateTopLevel(route: AppRoute) {
        navController.navigate(AppNavigationConfig.routeString(route)) {
            popUpTo(AppNavigationConfig.startGraphKey) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateBack(): Boolean {
        return navController.popBackStack()
    }
}

@Composable
fun rememberAppNavigator(
    navController: NavHostController = rememberMagiskNavController()
): AppNavigator {
    return remember(navController) {
        AppNavigator(navController)
    }
}
