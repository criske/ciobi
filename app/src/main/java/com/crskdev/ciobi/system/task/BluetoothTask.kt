package com.crskdev.ciobi.system.task

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus

/**
 * Created by Cristian Pela on 17.02.2020.
 */
@Suppress("UNUSED_PARAMETER")
object BluetoothTask {

    fun check(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val default = CiobiReport(CiobiTaskID.Bluetooth, "Bluetooth")
        context.sendBroadcastStatus(
            if (adapter == null) {
                default.copy(message = context.getString(R.string.feature_not_available_error))
            } else {
                val enabled = adapter.isEnabled
                default.copy(
                    status = if (enabled) CiobiReport.Status
                        .NEED_TO_FIX else CiobiReport.Status.OK,
                    message = if(enabled) context.getString(R.string.bluetooth_value) else null
                )
            }
        )
    }

    fun fix(context: Context) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && adapter.isEnabled) {
            adapter.disable()
        }
    }

    class BluetoothReceiver : BroadcastReceiver(), CiobiSystemObserverRegistrar {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                check(context)
            }
        }

        override fun register(context: Context) {
            context.registerReceiver(this, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
            check(context)
        }

        override fun unregister(context: Context) {
            context.unregisterReceiver(this)
        }

    }

}