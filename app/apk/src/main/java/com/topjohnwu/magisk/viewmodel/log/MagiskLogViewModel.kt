package com.topjohnwu.magisk.viewmodel.log

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.arch.uiText
import com.topjohnwu.magisk.core.AppContext
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.di.ServiceLocator
import com.topjohnwu.magisk.core.ktx.timeFormatStandard
import com.topjohnwu.magisk.core.ktx.toTime
import com.topjohnwu.magisk.core.repository.LogRepository
import com.topjohnwu.magisk.core.utils.MediaStoreUtils
import com.topjohnwu.magisk.core.utils.MediaStoreUtils.outputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.topjohnwu.magisk.core.R as CoreR

data class MagiskLogScreenUiState(
    val loading: Boolean = true,
    val logs: List<MagiskLogUiItem> = emptyList(),
    val filteredLogs: List<MagiskLogUiItem> = emptyList(),
    val stats: LogStats = LogStats.Empty,
    val filter: LogDisplayFilter = LogDisplayFilter.ALL,
    val searchQuery: String = "",
    val searchVisible: Boolean = false
)

data class LogStats(
    val total: Int, val issues: Int, val sources: Int
) {
    companion object {
        val Empty = LogStats(total = 0, issues = 0, sources = 0)

        fun from(items: List<MagiskLogUiItem>): LogStats {
            return LogStats(
                total = items.size,
                issues = items.count { it.isIssue },
                sources = items.map { it.sourceLabel }.distinct().size
            )
        }
    }
}

enum class LogDisplayFilter(@StringRes val labelRes: Int) {
    ALL(CoreR.string.log_filter_all), MAGISK(CoreR.string.log_filter_magisk), SU(CoreR.string.log_filter_su), ISSUES(
        CoreR.string.log_filter_issues
    )
}

enum class MagiskLogLevel(val code: Char, val shortLabel: String) {
    VERBOSE('V', "V"), DEBUG('D', "D"), INFO('I', "I"), WARN('W', "W"), ERROR('E', "E"), FATAL(
        'F',
        "F"
    ),
    UNKNOWN('?', "?");

    companion object {
        fun from(code: Char): MagiskLogLevel {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

data class MagiskLogUiItem(
    val id: Int,
    val timestamp: String,
    val tag: String,
    val level: MagiskLogLevel,
    val message: String,
    val raw: String,
    val pid: Int = 0,
    val tid: Int = 0
) {
    val isIssue: Boolean
        get() = level == MagiskLogLevel.WARN || level == MagiskLogLevel.ERROR || level == MagiskLogLevel.FATAL

    val isMagisk: Boolean
        get() = tag.contains("magisk", ignoreCase = true) || message.contains(
            "magisk",
            ignoreCase = true
        )

    val isSu: Boolean
        get() = message.contains("su:", ignoreCase = true) || raw.contains(
            "su:",
            ignoreCase = true
        ) || tag.equals("su", ignoreCase = true)

    val sourceLabel: String
        get() = when {
            isMagisk -> AppContext.getString(CoreR.string.log_source_magisk)
            isSu -> AppContext.getString(CoreR.string.log_source_su)
            tag.isNotBlank() -> tag
            else -> AppContext.getString(CoreR.string.log_source_system)
        }

    fun contains(query: String): Boolean {
        return tag.contains(query, ignoreCase = true) || message.contains(
            query,
            ignoreCase = true
        ) || raw.contains(query, ignoreCase = true) || timestamp.contains(query, ignoreCase = true)
    }
}

class MagiskLogViewModel(private val repo: LogRepository) : ViewModel() {
    private val _state = MutableStateFlow(MagiskLogScreenUiState())
    val state = _state.asStateFlow()

    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val messages = _messages.asSharedFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val raw = withContext(Dispatchers.IO) { repo.fetchMagiskLogs() }
            val items = withContext(Dispatchers.Default) {
                MagiskLogParser.parse(raw).mapIndexed { index, entry ->
                    MagiskLogUiItem(
                        id = index,
                        timestamp = entry.timestamp,
                        tag = entry.tag,
                        level = MagiskLogLevel.from(entry.level),
                        message = entry.message,
                        raw = entry.message,
                        pid = entry.pid,
                        tid = entry.tid
                    )
                }
            }
            _state.update { it.withLogs(items, loading = false) }
        }
    }

    fun setFilter(filter: LogDisplayFilter) {
        _state.update { it.withFilter(filter) }
    }

    fun setSearchQuery(query: String) {
        _state.update { it.withSearchQuery(query) }
    }

    fun clearSearch() {
        setSearchQuery("")
    }

    fun toggleSearch() {
        _state.update {
            if (it.searchVisible) {
                it.withSearchQuery("").copy(searchVisible = false)
            } else {
                it.copy(searchVisible = true)
            }
        }
    }

    fun clearMagiskLogs() {
        repo.clearMagiskLogs {
            _messages.tryEmit(uiText(CoreR.string.logs_cleared))
            refresh()
        }
    }

    fun saveMagiskLog() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val filename = "magisk_log_%s.log".format(
                    System.currentTimeMillis().toTime(timeFormatStandard)
                )
                val logFile = MediaStoreUtils.getFile(filename)
                val raw = repo.fetchMagiskLogs()
                logFile.uri.outputStream().bufferedWriter().use {
                    it.write("---Magisk Logs---\n${Info.env.versionString}\n\n$raw")
                }
                logFile.toString()
            }
            withContext(Dispatchers.Main) {
                result.onSuccess { _messages.emit(uiText(CoreR.string.saved_to_path, it)) }
                    .onFailure { _messages.emit(uiText(CoreR.string.failure)) }
            }
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return MagiskLogViewModel(ServiceLocator.logRepo) as T
            }
        }
    }
}

private fun MagiskLogScreenUiState.withLogs(
    logs: List<MagiskLogUiItem>, loading: Boolean
): MagiskLogScreenUiState {
    return copy(
        loading = loading,
        logs = logs,
        filteredLogs = logs.filteredBy(filter, searchQuery),
        stats = LogStats.from(logs)
    )
}

private fun MagiskLogScreenUiState.withFilter(filter: LogDisplayFilter): MagiskLogScreenUiState {
    return copy(
        filter = filter, filteredLogs = logs.filteredBy(filter, searchQuery)
    )
}

private fun MagiskLogScreenUiState.withSearchQuery(query: String): MagiskLogScreenUiState {
    return copy(
        searchQuery = query, filteredLogs = logs.filteredBy(filter, query)
    )
}

private fun List<MagiskLogUiItem>.filteredBy(
    filter: LogDisplayFilter, query: String
): List<MagiskLogUiItem> {
    val base = when (filter) {
        LogDisplayFilter.ALL -> this
        LogDisplayFilter.ISSUES -> filter { it.isIssue }
        LogDisplayFilter.MAGISK -> filter { it.isMagisk }
        LogDisplayFilter.SU -> filter { it.isSu }
    }
    return if (query.isBlank()) base else base.filter { it.contains(query) }
}
