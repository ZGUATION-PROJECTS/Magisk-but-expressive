package com.topjohnwu.magisk.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf

class AppNavigator internal constructor(
    private val backStack: SnapshotStateList<AppRoute>
) {

    fun navigate(route: AppRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.add(route)
        }
    }

    fun navigateTopLevel(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun navigateBack(): Boolean {
        if (backStack.size > 1) {
            backStack.removeLast()
            return true
        }
        return false
    }
}

@Composable
fun rememberAppNavigator(
    backStack: SnapshotStateList<AppRoute> = remember { mutableStateListOf(AppRoute.Home) }
): AppNavigator {
    return remember(backStack) {
        AppNavigator(backStack)
    }
}
