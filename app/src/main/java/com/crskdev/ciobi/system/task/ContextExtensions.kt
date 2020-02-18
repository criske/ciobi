package com.crskdev.ciobi.system.task

import android.content.Context
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskChecker
import com.crskdev.ciobi.domain.CiobiTaskID


/**
 * Created by Cristian Pela on 09.02.2020.
 */
fun Context.checkAllTasks() {
    CiobiTaskID.values().forEach {
        when(it){
            CiobiTaskID.WIFI,
            CiobiTaskID.MobileData -> NetworkTask.check(this)
            CiobiTaskID.ScreenBrightness -> BrightnessTask.check(this)
            CiobiTaskID.DownloadClutter -> DownloadClutterTask.check(this)
            CiobiTaskID.RecordClutter -> {}
            CiobiTaskID.DnD -> DnDTask.check(this)
            CiobiTaskID.Ringtone -> RingtoneTask.check(this)
            CiobiTaskID.Bluetooth -> BluetoothTask.check(this)
        }
    }
}
