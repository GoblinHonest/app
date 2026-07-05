package com.stark.miuix.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * HyperOS 系统适配 — 超级岛 + 公平运行内存
 *
 * 超级岛 (Super Island)：
 *   视频播放时通过 miui.focus.param 扩展参数发送岛通知，
 *   让用户无需离开当前 App 即可看到播放进度和控制。
 *   ⚠️ 正式上线需在小米开平申请场景审核，并替换 APP_ID。
 *
 * 公平运行内存 (Fair Memory)：
 *   监听 itgsa.intent.action.TRIM 广播，
 *   收到预警时主动释放 Coil 图片缓存，
 *   收到查杀广播时 3 秒内完成数据保存并回调系统。
 */
object HyperOsAdapter {

    // ─────────────────────────────────────────────
    // 超级岛
    // ─────────────────────────────────────────────

    private const val ISLAND_CHANNEL_ID = "cinehub_playback"
    private const val ISLAND_CHANNEL_NAME = "CineHub 播放状态"
    private const val ISLAND_NOTIFY_ID = 10086

    /**
     * 开始播放 → 发送超级岛通知
     *
     * 岛摘要态：显示正在播放的剧名 + 小圆点动效
     * 岛展开态：剧名 + 当前集数 + 进度
     */
    fun startPlaybackIsland(context: Context, title: String, episode: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道
        val channel = NotificationChannel(
            ISLAND_CHANNEL_ID, ISLAND_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "视频播放状态实时显示"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)

        // 构建超级岛 JSON 参数（摘要态：文本模板）
        val episodeText = if (episode.isNotBlank()) " · $episode" else ""
        val islandParams = """
        {
          "param_v2": {
            "business": "cinehub_playback",
            "updatable": true,
            "islandFirstFloat": true,
            "timeout": 720,
            "ticker": "CineHub · $title$episodeText",
            "aodTitle": "正在播放 $title",
            "param_island": {
              "islandProperty": 1,
              "bigIslandArea": {
                "title": "$title",
                "content": "$episode"
              },
              "smallIslandArea": {
                "content": "$title$episodeText"
              }
            }
          }
        }
        """.trimIndent()

        val notification = NotificationCompat.Builder(context, ISLAND_CHANNEL_ID)
            .setContentTitle("CineHub 正在播放")
            .setContentText("$title$episodeText")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setSilent(true)
            .build()

        // 附加超级岛参数（仅在 HyperOS 设备生效）
        notification.extras.putString("miui.focus.param", islandParams)

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            nm.notify(ISLAND_NOTIFY_ID, notification)
        }
    }

    /** 播放结束 → 取消超级岛通知 */
    fun stopPlaybackIsland(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ISLAND_NOTIFY_ID)
    }

    // ─────────────────────────────────────────────
    // 公平运行内存
    // ─────────────────────────────────────────────

    private const val ITGSA_ACTION = "itgsa.intent.action.TRIM"
    private const val TRANSACTION_EXCEPTION_REPLY = IBinder.FIRST_CALL_TRANSACTION

    private var mRemote: IBinder? = null
    private var mInitialized = false
    private var mHandler: Handler? = null

    /**
     * 在 Application.onCreate() 或 MainActivity.onCreate() 调用，
     * 注册内存预警广播监听。
     */
    fun initialize(context: Context) {
        synchronized(this) {
            if (mInitialized) return
            val ht = HandlerThread("HyperOsMemory")
            ht.start()
            mHandler = Handler(ht.looper)

            val filter = IntentFilter(ITGSA_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.applicationContext.registerReceiver(
                    memoryReceiver, filter, null, mHandler, Context.RECEIVER_EXPORTED
                )
            } else {
                context.applicationContext.registerReceiver(memoryReceiver, filter, null, mHandler)
            }
            mInitialized = true
            AppLogger.d("HyperOs", "公平运行内存适配已初始化")
        }
    }

    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            synchronized(this@HyperOsAdapter) {
                mRemote?.unlinkToDeath(this, 0)
                mRemote = null
            }
        }
    }

    private val memoryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ITGSA_ACTION) return
            val data = intent.extras ?: return
            val bundle = data.getBundle("common") ?: return

            val notifyType = bundle.getInt("notifyType")  // 1000=物理内存 2000=Java堆
            val notifyId = bundle.getInt("notifyId")
            val action = bundle.getString("action", "")   // "TRIM"=预警 "KILL"=查杀
            val callbackBinder = bundle.getBinder("callback")

            AppLogger.d("HyperOs", "内存广播: type=$notifyType action=$action")

            when (action) {
                "TRIM" -> {
                    // 内存预警 — 主动释放图片缓存
                    releaseMemoryCaches(context)
                    AppLogger.d("HyperOs", "已响应内存预警，释放图片缓存")
                }
                "KILL" -> {
                    // 查杀广播 — 3 秒内完成数据备份，回调系统
                    AppLogger.d("HyperOs", "收到查杀广播，开始数据备份")
                    // CineHub 无持久化需保存的临时状态，直接回调成功
                }
            }

            // 回调系统（result=0 表示成功处理）
            if (callbackBinder != null) {
                replyToSystem(notifyType, notifyId, callbackBinder, result = 0)
            }
        }
    }

    /** 主动释放内存缓存（Coil 内存缓存） */
    private fun releaseMemoryCaches(context: Context) {
        try {
            // 通知 Coil 单例清空内存缓存
            val loaderClass = Class.forName("coil3.SingletonImageLoader")
            val getMethod = loaderClass.getMethod("get", Context::class.java)
            val loader = getMethod.invoke(null, context.applicationContext)
            val memoryCacheField = loader?.javaClass?.getMethod("memoryCache")
            val cache = memoryCacheField?.invoke(loader)
            cache?.javaClass?.getMethod("clear")?.invoke(cache)
            AppLogger.d("HyperOs", "Coil 内存缓存已清空")
        } catch (e: Exception) {
            AppLogger.d("HyperOs", "Coil 缓存清空失败（非致命）: ${e.message}")
        }
    }

    /** 通过 Binder 回调系统，告知内存处理完成 */
    private fun replyToSystem(notifyType: Int, notifyId: Int, callback: IBinder, result: Int) {
        synchronized(this) {
            try {
                if (mRemote == null) {
                    mRemote = callback
                    mRemote?.linkToDeath(deathRecipient, 0)
                }
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeInt(notifyType)
                    data.writeInt(notifyId)
                    data.writeInt(result)
                    data.writeBundle(Bundle())
                    mRemote?.transact(TRANSACTION_EXCEPTION_REPLY, data, reply, IBinder.FLAG_ONEWAY)
                } finally {
                    reply.recycle()
                    data.recycle()
                }
            } catch (e: RemoteException) {
                AppLogger.d("HyperOs", "回调系统失败: ${e.message}")
                mRemote = null
            }
        }
    }
}
