package com.crskdev.ciobi.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.system.util.getAccountName
import com.crskdev.ciobi.ui.common.*
import kotlinx.android.synthetic.main.overlay_dialog.view.*

/**
 * Created by Cristian Pela on 11.02.2020.
 */
@SuppressLint("SetTextI18n")
@Suppress("FunctionName")
fun CiobiReportWindowOverlay(context: Context, onClose: (Any?) -> Unit): WindowOverlay<CiobiReport> {

    val size = withDensity {
        val system = Resources.getSystem()
        val dm = Resources.getSystem().displayMetrics
        val (wPx, hPx) = if(system.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            dm.widthPixels.px to dm.heightPixels.px
        }else{
            dm.heightPixels.px to dm.widthPixels.px
        }
        DpSize(wPx.toDp() - 50, hPx.toDp() / 2)
    }


    return WindowOverlay(context = context, size = size, onClose = onClose) {
        var currentReport: CiobiReport? = null
        onUpdate {
            currentReport = it
            textContent.text = it.message
        }
        onCreate {
            textTitle.text = (context.getAccountName()?.plus(": ") ?: "") + context.getString(R.string.app_name)
            btnClose.setOnClickListener {
                currentReport?.also {
                    context.sendBroadcastFix(it.id)
                    btnClose.isEnabled = false
                    close(it.id)
                }
            }
            btnIgnore.setOnClickListener {
                currentReport?.also {
                    close(it.id)
                }
            }
        }
        inflate(R.layout.overlay_dialog)
    }
}
