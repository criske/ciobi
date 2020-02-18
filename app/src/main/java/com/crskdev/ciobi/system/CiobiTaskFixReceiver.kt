package com.crskdev.ciobi.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.crskdev.ciobi.domain.CiobiTaskID.*
import com.crskdev.ciobi.system.task.*

/**
 * Created by Cristian Pela on 11.02.2020.
 */
class CiobiTaskFixReceiver : BroadcastReceiver(), CiobiSystemObserverRegistrar {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BroadcastContract.ACTION_FIX_RECEIVER) {
            when (valueOf(intent.getStringExtra(BroadcastContract.EXTRA)!!)) {
                WIFI -> NetworkTask.fixWifi(context)
                MobileData -> NetworkTask.fixMobileData(context)
                ScreenBrightness -> BrightnessTask.fix(context)
                DownloadClutter -> DownloadClutterTask.fix(context)
                RecordClutter -> { }
                DnD -> DnDTask.fix(context)
                Ringtone -> RingtoneTask.fix(context)
                Bluetooth -> BluetoothTask.fix(context)
            }
        }
    }

    override fun register(context: Context) {
        context.registerReceiver(
            this,
            IntentFilter(BroadcastContract.ACTION_FIX_RECEIVER)
        )
    }

    override fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }
}
