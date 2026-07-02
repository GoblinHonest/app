package com.stark.miuix.data.storage

import java.io.File

actual fun writeFile(path: String, content: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content)
}

actual fun readFile(path: String): String? {
    val file = File(path)
    return if (file.exists()) file.readText() else null
}

actual fun getAppDataDir(): String {
    val dir = File(System.getProperty("user.home"), ".miuix-video")
    dir.mkdirs()
    return dir.absolutePath
}
