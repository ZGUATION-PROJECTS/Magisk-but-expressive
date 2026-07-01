package com.topjohnwu.magisk.ui.component

/**
 * Authoritative public API map for the UI component package.
 *
 * Kotlin top-level declarations are imported directly from the package, so this
 * file is a registry of the supported component surface rather than a required
 * barrel export.
 */
object MagiskComponentExports {
    val layout = listOf(
        "MagiskScreenScaffold",
        "MagiskTopBar",
        "MagiskLazyContent",
        "MagiskContentColumn",
        "MagiskSection"
    )

    val cards = listOf(
        "MagiskCard",
        "MagiskOutlinedCard",
        "MagiskElevatedPanel",
        "MagiskActionCard"
    )

    val lists = listOf(
        "MagiskListItem",
        "MagiskSwitchItem",
        "MagiskExpandableListItem",
        "MagiskListItemDefaults"
    )

    val inputs = listOf(
        "MagiskSearchField",
        "MagiskFilterChipRow",
        "MagiskChipOption"
    )

    val dialogs = listOf(
        "MagiskDialog",
        "MagiskDialogAction",
        "MagiskBottomSheet",
        "MagiskDropdownMenu",
        "MagiskDropdownMenuItem"
    )

    val feedback = listOf(
        "MagiskSnackbarHost",
        "MagiskEmptyState",
        "MagiskLoadingState",
        "MagiskInlineMessage"
    )

    val text = listOf(
        "UiText.asString",
        "MagiskInfoPill",
        "MagiskStatusDot",
        "MagiskIconBadge"
    )

    val terminal = listOf(
        "MagiskTerminal",
        "MagiskTerminalActions",
        "MagiskTerminalButton"
    )
}
