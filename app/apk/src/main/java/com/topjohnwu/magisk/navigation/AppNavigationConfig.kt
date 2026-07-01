package com.topjohnwu.magisk.navigation

import android.net.Uri
import androidx.annotation.StringRes
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.R as CoreR

data class AppRouteSpec(
    val route: AppRoute,
    val graphKey: String,
    @StringRes val labelRes: Int,
    val topLevel: Boolean = false
)

object AppNavigationConfig {
    val startRoute: AppRoute = AppRoute.Home

    val routes: List<AppRouteSpec> = listOf(
        AppRouteSpec(AppRoute.Home, "home", CoreR.string.section_home, topLevel = true),
        AppRouteSpec(AppRoute.Superuser, "superuser", CoreR.string.superuser, topLevel = true),
        AppRouteSpec(AppRoute.Modules, "modules", CoreR.string.modules, topLevel = true),
        AppRouteSpec(AppRoute.Logs, "logs", CoreR.string.logs, topLevel = true),
        AppRouteSpec(AppRoute.Settings, "settings", CoreR.string.settings, topLevel = true),
        AppRouteSpec(AppRoute.Install, "install", CoreR.string.install),
        AppRouteSpec(AppRoute.DenyList, "denylist", CoreR.string.denylist),
        AppRouteSpec(AppRoute.Theme, "theme", CoreR.string.section_theme),
        AppRouteSpec(
            AppRoute.Flash(action = "{action}", additionalData = "{data}"),
            "flash/{action}?data={data}",
            CoreR.string.flash_screen_title
        ),
        AppRouteSpec(
            AppRoute.ModuleAction(idValue = "{id}", name = "{name}"),
            "module/{id}/action?name={name}",
            CoreR.string.module_action
        )
    )

    val topLevelRoutes: List<AppRouteSpec> = routes.filter { it.topLevel }
    val startGraphKey: String get() = routeString(startRoute)

    fun routeFromSection(section: String?): AppRoute = when (section) {
        Const.Nav.SUPERUSER -> AppRoute.Superuser
        Const.Nav.MODULES -> AppRoute.Modules
        Const.Nav.SETTINGS -> AppRoute.Settings
        else -> startRoute
    }

    fun specFor(route: AppRoute): AppRouteSpec {
        return routes.first { it.route.id == route.id }
    }

    fun routeString(route: AppRoute): String = when (route) {
        AppRoute.Home -> "home"
        AppRoute.Superuser -> "superuser"
        AppRoute.Modules -> "modules"
        AppRoute.Logs -> "logs"
        AppRoute.Settings -> "settings"
        AppRoute.Install -> "install"
        AppRoute.DenyList -> "denylist"
        AppRoute.Theme -> "theme"
        is AppRoute.Flash -> {
            val action = Uri.encode(route.action)
            val data = route.additionalData?.let(Uri::encode).orEmpty()
            "flash/$action?data=$data"
        }
        is AppRoute.ModuleAction -> {
            val id = Uri.encode(route.idValue)
            val name = Uri.encode(route.name)
            "module/$id/action?name=$name"
        }
    }
}
