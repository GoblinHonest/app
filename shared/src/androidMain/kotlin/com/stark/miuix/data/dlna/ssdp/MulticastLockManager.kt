/*
 * Copyright 2024 Stark Industries
 *
 * Android 多播锁管理
 *
 * SSDP 发现需要 WifiManager.MulticastLock 才能接收 UDP 多播包。
 * 默认 Android 会过滤非本机目的地址的多播流量以省电。
 * 此工具在投屏对话框打开时获取锁，关闭时释放。
 */
package com.stark.miuix.data.dlna.ssdp

import android.content.Context
import android.net.wifi.WifiManager

object MulticastLockManager {
    @Volatile
    private var multicastLock: WifiManager.MulticastLock? = null

    /** 获取多播锁（投屏发现前调用） */
    fun acquire(context: Context) {
        if (multicastLock?.isHeld == true) return
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return
        multicastLock = wifiManager.createMulticastLock("miuix-dlna-ssdp").apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    /** 释放多播锁（投屏发现结束后调用） */
    fun release() {
        runCatching { multicastLock?.release() }
        multicastLock = null
    }
}
