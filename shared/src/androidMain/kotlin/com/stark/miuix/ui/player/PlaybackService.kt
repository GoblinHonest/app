/*
 * Copyright 2024 Stark Industries
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stark.miuix.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.stark.miuix.util.AppLogger

/**
 * 后台播放服务
 *
 * 使用 MediaSession 实现后台播放 + 通知栏控制。
 * 当用户开启"后台播放"设置后，退出播放器页面时视频继续播放，
 * 通知栏显示播放/暂停/上一集/下一集控制。
 */
class PlaybackService : Service() {

    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        AppLogger.d("PlaybackService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            ACTION_NEXT -> handleNext()
            ACTION_PREV -> handlePrev()
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d("PlaybackService", "Service destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "视频播放",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台视频播放控制"
                setShowBadge(false)
            }
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, episodeName: String, isPlaying: Boolean) {
        val notification = buildNotification(title, episodeName, isPlaying)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(title: String, episodeName: String, isPlaying: Boolean): Notification {
        val playPauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, PlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 1,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 2,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 3,
            Intent(this, PlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(episodeName)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(android.R.drawable.ic_media_previous, "上一集", prevIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "暂停" else "播放",
                playPauseIntent
            )
            .addAction(android.R.drawable.ic_media_next, "下一集", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "停止", stopIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .setOngoing(isPlaying)
            .build()
    }

    private fun handlePlay() {
        AppLogger.d("PlaybackService", "Play")
        PlayerStore.exoPlayer?.play()
        PlayerStore.isPlaying = true
    }

    private fun handlePause() {
        AppLogger.d("PlaybackService", "Pause")
        PlayerStore.exoPlayer?.pause()
        PlayerStore.isPlaying = false
    }

    private fun handleNext() {
        AppLogger.d("PlaybackService", "Next")
    }

    private fun handlePrev() {
        AppLogger.d("PlaybackService", "Prev")
    }

    companion object {
        private const val CHANNEL_ID = "cinehub_playback"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.stark.miuix.action.PLAY"
        const val ACTION_PAUSE = "com.stark.miuix.action.PAUSE"
        const val ACTION_NEXT = "com.stark.miuix.action.NEXT"
        const val ACTION_PREV = "com.stark.miuix.action.PREV"
        const val ACTION_STOP = "com.stark.miuix.action.STOP"
    }
}
