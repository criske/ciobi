package com.crskdev.ciobi.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LifecycleObserver
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskChecker
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.domain.CiobiTaskManager
import com.crskdev.ciobi.system.task.checkAllTasks
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.scan

/**
 * Created by Cristian Pela on 11.02.2020.
 */
@FlowPreview
@ExperimentalCoroutinesApi
class CiobiReportUIReceiver(private val context: Context):
    CiobiTaskManager, LifecycleObserver {

    override fun observeReports(): Flow<List<CiobiReport>> = broadcastFlow()
        .scan(emptyList()) { acc, curr ->
            coroutineScope {
                when (curr) {
                    is Action.CheckAll -> {
                        acc // todo remove check all don't need anymore
                    }
                    is Action.ReportReceived -> {
                        acc.replaceOrAdd(curr.report)
                    }
                }
            }
        }

    private fun broadcastFlow(): Flow<Action> = callbackFlow {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val report = intent.getStringExtra(BroadcastContract.EXTRA)!!.deserializeToReport()
                if(report.redirectIntent != null){
                    context.redirect(report.redirectIntent)
                }else {
                    offer(Action.ReportReceived(report))
                }
                resultCode = BroadcastContract.REPORT_IS_PROCESSED
            }
        }.apply {
            context.registerReceiver(
                this,
                IntentFilter(BroadcastContract.ACTION_STATUS_REPORT_PROCESS_RECEIVER)
            )
        }
        context.checkAllTasks()
        awaitClose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    override fun execute(taskID: CiobiTaskID) {
        context.sendBroadcastFix(taskID)
    }

    private sealed class Action {
        object CheckAll : Action()
        class ReportReceived(val report: CiobiReport) : Action()
    }

    private fun List<CiobiReport>.replaceOrAdd(report: CiobiReport): List<CiobiReport> =
        if (!any { it.id == report.id })
            this + report
        else
            map { if (it.id == report.id) report else it }
}