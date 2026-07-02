package com.stark.miuix.data.storage

import java.io.File

actual fun writeFile(path: String, content: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content, Charsets.UTF_8)
}

actual fun readFile(path: String): String? {
    val file = File(path)
    return if (file.exists()) file.readText(Charsets.UTF_8) else null
}

/**
 * Android 数据目录
 *
 * 使用可变属性允许 MainActivity 在启动时注入 context.filesDir。
 * 未注入时回退到 user.home（Desktop JVM 兼容）。
 */
var androidDataDir: String? = null

actual fun getAppDataDir(): String {
    val base = androidDataDir ?: System.getProperty("user.home") ?: "/tmp"
    val dir = File(base, "miuix-video")
    dir.mkdirs()
    return dir.absolutePath
}
