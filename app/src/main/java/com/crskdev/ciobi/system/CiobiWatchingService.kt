package com.crskdev.ciobi.system

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.HandlerCompat
import com.crskdev.ciobi.MainActivity
import com.crskdev.ciobi.R
import com.crskdev.ciobi.system.task.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.concurrent.TimeUnit

@FlowPreview
@ExperimentalCoroutinesApi
class CiobiWatchingService : Service() {

    companion object {
        private const val CHANNEL_ID = "CiobiWatchingService"
        private const val ONGOING_NOTIFICATION_ID: Int = 100
        fun startInForeground(context: Context) {
            val safeContext = context.applicationContext
            ContextCompat.startForegroundService(
                safeContext,
                Intent(safeContext, CiobiWatchingService::class.java)
            )
        }
    }

    private val systemObserverRegistry = mutableListOf<CiobiSystemObserverRegistrar>()

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        val context = applicationContext
        with(systemObserverRegistry) {
            add(ScreenOnOffBroadcastReceiver(applicationContext).apply { register(context) })
            add(CiobiTaskFixReceiver().apply { register(context) })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()

        return START_STICKY
    }

    override fun onDestroy() {
        systemObserverRegistry.forEach { it.unregister(applicationContext) }
    }

    private fun startInForeground() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java)
                .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
                .let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Ciobi Watching Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService<NotificationManager>()?.createNotificationChannel(serviceChannel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_child_friendly_black_24dp)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    class SystemBootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "android.intent.action.BOOT_COMPLETED")
                startInForeground(context)
        }
    }

    class ScreenOnOffBroadcastReceiver(context: Context) : BroadcastReceiver(),
        CiobiSystemObserverRegistrar {

        private var backgroundHandlerThread: HandlerThread =
            HandlerThread("Ciobi Background Handler").apply { start() }
        private var backgroundHandler: Handler
        private val ciobiTaskContentObservers = mutableListOf<CiobiSystemObserverRegistrar>()

        companion object {
            private const val DELAY_TOKEN = "DELAY_TOKEN"
        }

        init {
            backgroundHandler = Handler(backgroundHandlerThread.looper)
            startWatching(context, false)
        }

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    startWatching(context)
                }
                Intent.ACTION_SCREEN_OFF -> {
                    stopWatching(context)
                }
            }
        }

        private fun stopWatching(context: Context) {
            ciobiTaskContentObservers.forEach { it.unregister(context) }
            ciobiTaskContentObservers.clear()
            backgroundHandler.removeCallbacksAndMessages(DELAY_TOKEN)
        }

        private fun startWatching(context: Context, delayed: Boolean = true) {
            val runnable = Runnable {
                with(ciobiTaskContentObservers) {
                    add(NetworkTask.NetworkObserver(context)
                        .apply { register(context) })
                    val delegate = CiobiTaskContentObserverDelegate(backgroundHandler)
                        .apply { register(context) }
                    add(delegate)
                    add(BrightnessTask.BrightnessContentObserver(context, delegate)
                        .apply { register(context) })
                    add(RingtoneTask.RingtoneContentObserver(context, delegate)
                        .apply { register(context) })
                    add(RingtoneTask.RingtoneModeReceiver()
                        .apply { register(context) })
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        add(DnDTask.DnDBroadcastReceiver().apply { register(context) })
                    }
                    add(DownloadClutterTask.DownloadClutterReceiver(context)
                            .apply { register(context) })
                    add(BluetoothTask.BluetoothReceiver().apply { register(context) })
                    // add(DownloadClutterTask.DownloadClutterReceiver2(context, backgroundHandler).apply { register(context) })
                }
            }
            if (delayed) {
                backgroundHandler.removeCallbacksAndMessages(DELAY_TOKEN)
                HandlerCompat.postDelayed(
                    backgroundHandler,
                    runnable,
                    DELAY_TOKEN,
                    TimeUnit.SECONDS.toMillis(10)
                )
            } else {
                runnable.run()
            }
        }

        override fun register(context: Context) {
            context.registerReceiver(this, IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            })
        }

        override fun unregister(context: Context) {
            context.unregisterReceiver(this)
            backgroundHandlerThread.quit()
        }

    }

}
