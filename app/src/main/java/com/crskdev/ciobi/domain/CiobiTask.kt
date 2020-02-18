package com.crskdev.ciobi.domain

/**
 * Created by Cristian Pela on 09.02.2020.
 */

interface CiobiTaskChecker {
    val default: CiobiReport
    fun current(): CiobiReport
}

enum class CiobiTaskID {
    WIFI,
    MobileData,
    ScreenBrightness,
    DownloadClutter,
    RecordClutter,
    DnD,
    Ringtone,
    Bluetooth
}