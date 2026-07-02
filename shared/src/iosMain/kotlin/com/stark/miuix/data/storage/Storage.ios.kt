package com.stark.miuix.data.storage

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual fun writeFile(path: String, content: String) {
    val nsContent = NSString.create(string = content)
    val data = nsContent.dataUsingEncoding(NSUTF8StringEncoding) ?: return
    val fileManager = NSFileManager.defaultManager
    val parentPath = path.substringBeforeLast("/")
    if (!fileManager.fileExistsAtPath(parentPath)) {
        fileManager.createDirectoryAtPath(parentPath, withIntermediateDirectories = true, attributes = null, error = null)
    }
    data.writeToFile(path, atomically = true)
}

@OptIn(ExperimentalForeignApi::class)
actual fun readFile(path: String): String? {
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(path)) return null
    val data = fileManager.contentsAtPath(path) ?: return null
    return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
}

@OptIn(ExperimentalForeignApi::class)
actual fun getAppDataDir(): String {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, inDomains = NSUserDomainMask)
    val docUrl = urls.firstOrNull() as? NSURL
    val base = docUrl?.path ?: "/tmp"
    val dir = "$base/miuix-video"
    if (!fileManager.fileExistsAtPath(dir)) {
        fileManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
    }
    return dir
}
