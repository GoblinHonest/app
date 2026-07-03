package com.stark.miuix.util

import com.stark.miuix.data.storage.getAppDataDir
import com.stark.miuix.data.storage.writeFile
import com.stark.miuix.data.storage.readFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 应用日志系统
 *
 * 日志同时输出到 println（logcat）和本地文件，方便远程调试。
 * 日志文件：{appDataDir}/app.log
 */
object AppLogger {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val logFilePath by lazy {
        try { "${getAppDataDir()}/app.log" } catch (_: Exception) { "" }
    }

    fun d(tag: String, msg: String) {
        addLog("D/$tag: $msg")
    }

    fun e(tag: String, msg: String, error: Throwable? = null) {
        val errorInfo = error?.let { "\n  ${it::class.simpleName}: ${it.message}\n  ${it.stackTraceToString().take(500)}" } ?: ""
        addLog("E/$tag: $msg$errorInfo")
    }

    fun getLogFile(): String = logFilePath

    fun getLogContent(): String {
        return try { readFile(logFilePath) ?: "" } catch (_: Exception) { "" }
    }

    fun clear() {
        _logs.value = emptyList()
        try { if (logFilePath.isNotBlank()) writeFile(logFilePath, "") } catch (_: Exception) {}
    }

    private fun addLog(entry: String) {
        val timestamped = "[${currentTimeMillis()}] $entry"
        val current = _logs.value.toMutableList()
        current.add(0, timestamped)
        if (current.size > 500) current.subList(500, current.size).clear()
        _logs.value = current
        println(timestamped)

        try {
            if (logFilePath.isNotBlank()) {
                val existing = readFile(logFilePath) ?: ""
                val trimmed = if (existing.length > 100_000) existing.take(50_000) else existing
                writeFile(logFilePath, "$timestamped\n$trimmed")
            }
        } catch (_: Exception) {}
    }
}
