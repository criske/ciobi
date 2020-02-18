package com.crskdev.ciobi.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.util.SettingsSystemCompat
import com.crskdev.ciobi.ui.common.WindowOverlay


/**
 * Created by Cristian Pela on 11.02.2020.
 */
object CiobiReportOverlayReceiver : BroadcastReceiver() {

    private val windows = mutableMapOf<CiobiTaskID, WindowOverlay<CiobiReport>>()

    override fun onReceive(context: Context, intent: Intent) {
        if (resultCode == BroadcastContract.REPORT_IS_PROCESSED) return

        val report = intent.getStringExtra(BroadcastContract.EXTRA)!!.deserializeToReport()

        when (report.status) {
            CiobiReport.Status.NEED_TO_FIX -> {
                if (SettingsSystemCompat.canDrawOverlays(context)) {
                    val isOpened = windows[report.id]?.isCreating ?: false
                    if (!isOpened) {
                        val window = CiobiReportWindowOverlay(context) {
                            if (it is CiobiTaskID) windows.remove(it)
                        }
                        window.create()
                        windows[report.id] = window
                    }
                    windows[report.id]!!.update(report)
                } else {
                    SettingsSystemCompat.ACTION_MANAGE_OVERLAY_PERMISSION
                        ?.let { context.redirect(it) }
                }
            }
            CiobiReport.Status.OK -> {
                windows[report.id]?.destroy(report.id)
            }
            CiobiReport.Status.ERROR -> {
                report.redirectIntent?.run {
                    context.redirect(this)
                }
            }
            else -> Unit
        }
    }

}