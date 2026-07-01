package com.topjohnwu.magisk.arch

import androidx.annotation.StringRes

sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText
}

fun uiText(value: String): UiText = UiText.Plain(value)

fun uiText(@StringRes resId: Int, vararg args: Any): UiText =
    UiText.Resource(resId, args.toList())
