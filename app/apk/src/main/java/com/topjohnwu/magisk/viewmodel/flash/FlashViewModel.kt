package com.topjohnwu.magisk.viewmodel.flash

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.topjohnwu.magisk.arch.TerminalLogStore
import com.topjohnwu.magisk.arch.UiEffect
import com.topjohnwu.magisk.arch.UiText
import com.topjohnwu.magisk.arch.uiText
import com.topjohnwu.magisk.core.AppContext
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.ktx.timeFormatStandard
import com.topjohnwu.magisk.core.ktx.toTime
import com.topjohnwu.magisk.core.ktx.writeTo
import com.topjohnwu.magisk.core.tasks.MagiskInstaller
import com.topjohnwu.magisk.core.utils.MediaStoreUtils
import com.topjohnwu.magisk.core.utils.MediaStoreUtils.displayName
import com.topjohnwu.magisk.core.utils.MediaStoreUtils.inputStream
import com.topjohnwu.magisk.core.utils.MediaStoreUtils.outputStream
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import com.topjohnwu.magisk.core.R as CoreR

data class FlashUiState(
    val running: Boolean = false,
    val success: Boolean = false,
    val showReboot: Boolean = Info.isRooted
)

class FlashViewModel : ViewModel() {
    private val _state = MutableStateFlow(FlashUiState())
    val state: StateFlow<FlashUiState> = _state.asStateFlow()

    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val messages: SharedFlow<UiText> = _messages.asSharedFlow()

    private val _effects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private val terminal = TerminalLogStore()
    val lines = terminal.lines
    private var started = false

    fun start(action: String, uri: Uri?) {
        if (started) return
        started = true
        viewModelScope.launch {
            _state.update { it.copy(running = true, success = false, showReboot = Info.isRooted) }
            if (requiresRoot(action)) {
                val hasRoot = withContext(Dispatchers.IO) { Shell.getShell().isRoot }
                if (!hasRoot) {
                    terminal.addLine("! ${AppContext.getString(CoreR.string.root_required_operation)}")
                    _state.update { it.copy(running = false, success = false, showReboot = false) }
                    _messages.emit(uiText(CoreR.string.failure))
                    return@launch
                }
            }
            val result = when (action) {
                Const.Value.FLASH_ZIP -> if (uri == null) false else flashZipWithLogs(uri)
                Const.Value.UNINSTALL -> {
                    _state.update { it.copy(showReboot = false) }
                    MagiskInstaller.Uninstall(terminal.console, terminal.logs).exec()
                }
                Const.Value.FLASH_MAGISK -> if (Info.isEmulator) {
                    MagiskInstaller.Emulator(terminal.console, terminal.logs).exec()
                } else {
                    MagiskInstaller.Direct(terminal.console, terminal.logs).exec()
                }
                Const.Value.FLASH_INACTIVE_SLOT -> {
                    _state.update { it.copy(showReboot = false) }
                    MagiskInstaller.SecondSlot(terminal.console, terminal.logs).exec()
                }
                Const.Value.PATCH_FILE -> if (uri == null) false else {
                    _state.update { it.copy(showReboot = false) }
                    MagiskInstaller.Patch(uri, terminal.console, terminal.logs).exec()
                }
                else -> false
            }
            terminal.sync()
            _state.update { it.copy(running = false, success = result) }
        }
    }

    private suspend fun flashZipWithLogs(uri: Uri): Boolean {
        val installDir = File(AppContext.cacheDir, "flash")
        val prep: Pair<Int?, Triple<File, File, String>?> = withContext(Dispatchers.IO) {
            try {
                installDir.deleteRecursively()
                installDir.mkdirs()

                val zipFile = if (uri.scheme == "file") {
                    uri.toFile()
                } else {
                    File(installDir, "install.zip").also {
                        try {
                            uri.inputStream().writeTo(it)
                        } catch (e: IOException) {
                            val message = if (e is FileNotFoundException) {
                                CoreR.string.flash_invalid_uri
                            } else {
                                CoreR.string.flash_copy_to_cache_failed
                            }
                            return@withContext message to null
                        }
                    }
                }

                val binary = File(installDir, "update-binary")
                AppContext.assets.open("module_installer.sh").use { it.writeTo(binary) }

                val name = uri.displayName
                null to Triple(installDir, zipFile, name)
            } catch (e: Exception) {
                Timber.e(e)
                CoreR.string.flash_extract_failed to null
            }
        }

        val (errorRes, prepResult) = prep
        if (prepResult == null) {
            terminal.addLine(errorLine(errorRes ?: CoreR.string.flash_installation_failed))
            return false
        }

        val (dir, zipFile, displayName) = prepResult
        terminal.addLine(AppContext.getString(CoreR.string.flash_installing_file, displayName))

        val success = withContext(Dispatchers.IO) {
            Shell.cmd("sh $dir/update-binary dummy 1 '${zipFile.absolutePath}'")
                .to(terminal.console, terminal.logs)
                .exec()
                .isSuccess
        }
        if (!success) terminal.addLine(errorLine(CoreR.string.flash_installation_failed))

        Shell.cmd("cd /", "rm -rf $dir ${Const.TMPDIR}").submit()
        return success
    }

    fun saveLog() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val name = "magisk_install_log_%s.log".format(
                    System.currentTimeMillis().toTime(timeFormatStandard)
                )
                val file = MediaStoreUtils.getFile(name)
                file.uri.outputStream().bufferedWriter().use(terminal::writeTo)
                file.toString()
            }.onSuccess { path ->
                _messages.emit(uiText(CoreR.string.saved_to_path, path))
            }.onFailure {
                _messages.emit(uiText(CoreR.string.failure))
            }
        }
    }

    fun rebootNow() {
        _effects.tryEmit(UiEffect.Reboot)
    }

    private fun requiresRoot(action: String): Boolean =
        action == Const.Value.FLASH_ZIP ||
            action == Const.Value.UNINSTALL ||
            action == Const.Value.FLASH_MAGISK ||
            action == Const.Value.FLASH_INACTIVE_SLOT

    private fun errorLine(@StringRes res: Int): String =
        "! ${AppContext.getString(res)}"

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FlashViewModel() as T
            }
        }
    }
}

internal fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"
