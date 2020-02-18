package com.crskdev.ciobi.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.crskdev.ciobi.MainActivity
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import org.json.JSONObject

/**
 * Created by Cristian Pela on 11.02.2020.
 */
object BroadcastContract {

    const val ACTION_STATUS_REPORT_PROCESS_RECEIVER = "ACTION_STATUS_REPORT_PROCESS_RECEIVER"
    const val ACTION_FIX_RECEIVER = "ACTION_FIX_RECEIVER"

    const val EXTRA = "EXTRA"

    const val REPORT_IS_PROCESSED = 1

    const val REPORT_IS_NOT_PROCESSED = 2

}

fun Context.sendBroadcastFix(id: CiobiTaskID): Unit = Intent().apply {
    action = BroadcastContract.ACTION_FIX_RECEIVER
    putExtra(BroadcastContract.EXTRA, id.name)
}.let { sendBroadcast(it) }

fun Context.sendBroadcastStatus(report: CiobiReport): Unit = Intent().apply {
    action = BroadcastContract.ACTION_STATUS_REPORT_PROCESS_RECEIVER
    putExtra(BroadcastContract.EXTRA, report.serialize())
}.let {
    sendOrderedBroadcast(it, null, CiobiReportOverlayReceiver, null,
        BroadcastContract.REPORT_IS_NOT_PROCESSED, null, null)
}

fun Context.redirect(intentValue: String){
    val context = applicationContext
    try {
        startActivity(Intent(intentValue).run {
            data = Uri.parse("package:" + context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        })
    } catch (ex: Exception) {
        try {
            startActivity(Intent()
                .setClassName(context, intentValue)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_CLEAR_TOP)))
        } catch (ex: Exception) {
            Toast.makeText(
                context,
                "Error: Activity doesn't exists for $intentValue",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun CiobiReport.serialize(): String =
    JSONObject().apply {
        put("id", id.name)
        put("name", name)
        put("status", status.name)
        put("message", message ?: "")
        put("redirect_intent", redirectIntent ?: "")
    }.toString()


fun String.deserializeToReport(): CiobiReport {
    val jsonObject = JSONObject(this)
    return CiobiReport(
        CiobiTaskID.valueOf(jsonObject.getString("id")),
        jsonObject.getString("name"),
        CiobiReport.Status.valueOf(jsonObject.getString("status")),
        jsonObject.getString("message").takeIf { it.isNotBlank() },
        jsonObject.getString("redirect_intent").takeIf { it.isNotBlank() }
    )
}

