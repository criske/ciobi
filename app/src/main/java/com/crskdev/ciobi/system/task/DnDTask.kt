package com.crskdev.ciobi.system.task

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus

/**
 * Created by Cristian Pela on 13.02.2020.
 */
object DnDTask {

    fun fix(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService<NotificationManager>()!!
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else {
                context.sendBroadcastStatus(
                    CiobiReport(
                        CiobiTaskID.DnD,
                        context.getString(R.string.ring_name),
                        CiobiReport.Status.ERROR,
                        context.getString(
                            R.string.redirect_error_enable_feature
                        ),
                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    )
                )
            }
        }
    }

    fun check(context: Context) {
        var report = CiobiReport(
            id = CiobiTaskID.DnD,
            name = context.getString(R.string.dnd_name),
            status = CiobiReport.Status.OK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationManager = context.getSystemService<NotificationManager>()!!
            if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                report = report.copy(
                    status = CiobiReport.Status.NEED_TO_FIX,
                    message = context.getString(R.string.dnd_value)
                )
            }
        } else {
            report = report.copy(
                message = context.getString(R.string.feature_not_available_error),
                status = CiobiReport.Status.PENDING
            )
        }
        context.sendBroadcastStatus(report)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    class DnDBroadcastReceiver : BroadcastReceiver(), CiobiSystemObserverRegistrar {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED) {
                check(context)
            }
        }
        override fun register(context: Context) {
            context.registerReceiver(
                this,
                IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            )
            check(context)
        }
        override fun unregister(context: Context) {
            context.unregisterReceiver(this)
        }
    }
}