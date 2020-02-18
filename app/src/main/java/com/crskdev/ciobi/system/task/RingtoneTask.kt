package com.crskdev.ciobi.system.task

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.provider.Settings
import androidx.core.content.getSystemService
import androidx.core.os.HandlerCompat
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus
import kotlin.math.roundToInt


/**
 * Created by Cristian Pela on 12.02.2020.
 */
object RingtoneTask {

    private const val THRESHOLD = 0.7

    fun fix(context: Context) {
        val audioManager = context.getSystemService<AudioManager>()!!
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_RING,
                (maxVolume * THRESHOLD).roundToInt(),
                0
            )
            if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
        } catch (ex: Exception) {
            //might be in do not disturb mode
            DnDTask.fix(context)
        }
    }

    fun check(context: Context) {
        val report = CiobiReport(
            CiobiTaskID.Ringtone,
            context.getString(R.string.ring_name),
            CiobiReport.Status.OK
        )
        val audioManager = context.getSystemService<AudioManager>()!!
        val volumeRatio = calculateVolumeRatio(audioManager)
        var fixMessage =
            if (volumeRatio < THRESHOLD) context.getString(R.string.ring_value_low) else ""
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> {
                fixMessage += (if (fixMessage.isNotEmpty()) "\n" else "") + context.getString(
                    R.string.ring_value_silent
                )
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                fixMessage += (if (fixMessage.isNotEmpty()) "\n" else "") + context.getString(
                    R.string.ring_value_vibrate
                )
            }
        }
        context.sendBroadcastStatus(
            if (fixMessage.isNotEmpty()) report.copy(
                status = CiobiReport.Status.NEED_TO_FIX,
                message = fixMessage
            ) else report
        )
    }

    private fun calculateVolumeRatio(audioManager: AudioManager): Float {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        return currentVolume / maxVolume.toFloat()
    }

    class RingtoneContentObserver(private val context: Context, private val delegate: CiobiTaskContentObserverDelegate) :
        CiobiSystemObserverRegistrar, CiobiSystemObserverRegistrar.Delegated {

        private val uri = Settings.System.getUriFor("volume_ring_speaker")

        companion object {
            private const val CHANGE_TOKEN = "CHANGE_RING_TONE_VOLUME"
        }

        override fun onChange() {
            val runnable = Runnable { check(context) }
            delegate.handler.removeCallbacksAndMessages(CHANGE_TOKEN)
            HandlerCompat.postDelayed(delegate.handler, runnable, CHANGE_TOKEN, 1000)
        }

        override fun register(context: Context) {
            delegate.registerDelegated(uri, this)
            check(context)
        }

        override fun unregister(context: Context) {
            delegate.unregisterDelegated(uri)
        }
    }


    class RingtoneModeReceiver : BroadcastReceiver(), CiobiSystemObserverRegistrar {

        override fun onReceive(context: Context, intent: Intent) {
            check(context)
        }

        override fun register(context: Context) {
            context.registerReceiver(this, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION))
            check(context)
        }

        override fun unregister(context: Context) {
            context.unregisterReceiver(this)
        }

    }


}