package com.stark.miuix.data.storage

import kotlinx.browser.localStorage

actual fun writeFile(path: String, content: String) {
    localStorage.setItem(path, content)
}

actual fun readFile(path: String): String? {
    return localStorage.getItem(path)
}

actual fun getAppDataDir(): String = "miuix-video"
