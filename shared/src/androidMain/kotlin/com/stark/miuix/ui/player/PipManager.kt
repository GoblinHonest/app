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

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import com.stark.miuix.util.AppLogger

/**
 * 画中画模式管理器
 *
 * 支持 Android 8.0+ 的 Picture-in-Picture 模式。
 * 用户按 Home 键或点击 PiP 按钮时，视频缩小为悬浮窗继续播放。
 */
object PipManager {

    /** 检查设备是否支持画中画 */
    fun isSupported(activity: Activity): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    /** 进入画中画模式 */
    fun enterPip(activity: Activity, aspectRatio: Rational = Rational(16, 9)): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        if (!isSupported(activity)) return false

        return try {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            activity.enterPictureInPictureMode(params)
        } catch (e: Exception) {
            AppLogger.e("PipManager", "Enter PiP failed: ${e.message}")
            false
        }
    }

    /** 更新画中画参数（如宽高比变化） */
    fun updateParams(activity: Activity, aspectRatio: Rational = Rational(16, 9)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            activity.setPictureInPictureParams(params)
        } catch (e: Exception) {
            AppLogger.e("PipManager", "Update PiP params failed: ${e.message}")
        }
    }
}
