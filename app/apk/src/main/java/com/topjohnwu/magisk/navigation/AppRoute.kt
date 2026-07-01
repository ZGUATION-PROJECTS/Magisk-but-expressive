package com.topjohnwu.magisk.navigation

sealed interface AppRoute {
    val id: String

    data object Home : AppRoute {
        override val id = "home"
    }

    data object Superuser : AppRoute {
        override val id = "superuser"
    }

    data object Modules : AppRoute {
        override val id = "modules"
    }

    data object Logs : AppRoute {
        override val id = "logs"
    }

    data object Settings : AppRoute {
        override val id = "settings"
    }

    data object Install : AppRoute {
        override val id = "install"
    }

    data object DenyList : AppRoute {
        override val id = "denylist"
    }

    data object Theme : AppRoute {
        override val id = "theme"
    }

    data class Flash(
        val action: String,
        val additionalData: String? = null
    ) : AppRoute {
        override val id = "flash"
    }

    data class ModuleAction(
        val idValue: String,
        val name: String
    ) : AppRoute {
        override val id = "module_action"
    }
}
