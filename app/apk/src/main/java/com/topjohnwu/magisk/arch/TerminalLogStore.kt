package com.topjohnwu.magisk.arch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Writer
import java.util.Collections

class TerminalLogStore {
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    val console: MutableList<String> = Collections.synchronizedList(mutableListOf())
    val logs: MutableList<String> = Collections.synchronizedList(mutableListOf())

    fun addLine(line: String) {
        console += line
        sync()
    }

    fun sync() {
        _lines.value = console.toList()
    }

    fun writeTo(writer: Writer) {
        console.forEach { writer.write("$it\n") }
        if (logs.isNotEmpty()) {
            writer.write("\n")
            logs.forEach { writer.write("$it\n") }
        }
    }
}
