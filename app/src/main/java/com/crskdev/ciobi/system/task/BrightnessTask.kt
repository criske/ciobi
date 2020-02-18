package com.crskdev.ciobi.system.task

import android.content.Context
import android.provider.Settings
import androidx.core.os.HandlerCompat
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus
import com.crskdev.ciobi.system.util.SettingsSystemCompat

/**
 * Created by Cristian Pela on 12.02.2020.
 */
object BrightnessTask {

    private const val THRESHOLD = 20

    fun fix(context: Context) {
        if (SettingsSystemCompat.canWrite(context)) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, THRESHOLD
            )
        } else {
            context.sendBroadcastStatus(
                CiobiReport(
                    id = CiobiTaskID.ScreenBrightness,
                    name = context.getString(R.string.screen_brightness_name),
                    status = CiobiReport.Status.ERROR,
                    redirectIntent = SettingsSystemCompat.ACTION_MANAGE_WRITE_SETTINGS,
                    message = context.getString(R.string.redirect_error_enable_feature)
                )
            )
        }
    }

    fun check(context: Context) {
        val brightness =
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 0)
        context.sendBroadcastStatus(
            if (brightness > 20) {
                CiobiReport(
                    id = CiobiTaskID.ScreenBrightness,
                    name = context.getString(R.string.screen_brightness_name),
                    message = context.getString(R.string.screen_brightness_value, brightness, 20),
                    status = CiobiReport.Status.NEED_TO_FIX
                )
            } else {
                CiobiReport(
                    id = CiobiTaskID.ScreenBrightness,
                    name = context.getString(R.string.screen_brightness_name),
                    status = CiobiReport.Status.OK
                )
            }
        )
    }

    class BrightnessContentObserver(
        private val context: Context,
        private val delegate: CiobiTaskContentObserverDelegate) :
        CiobiSystemObserverRegistrar, CiobiSystemObserverRegistrar.Delegated {

        companion object {
            private const val CHANGE_TOKEN = "CHANGE_TOKEN_BRIGHTNESS"
        }

        private val uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS)

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
}