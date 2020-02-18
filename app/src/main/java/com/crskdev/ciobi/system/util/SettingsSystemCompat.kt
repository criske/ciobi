package com.crskdev.ciobi.system.util

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Created by Cristian Pela on 12.02.2020.
 */
object SettingsSystemCompat {

    fun canWrite(context: Context): Boolean = onMinimumVersion(true){
        Settings.System.canWrite(context)
    }

    fun canDrawOverlays(context: Context): Boolean = onMinimumVersion(true){
        Settings.canDrawOverlays(context)
    }

    val ACTION_MANAGE_WRITE_SETTINGS: String? = onMinimumVersion(null) {
        Settings.ACTION_MANAGE_WRITE_SETTINGS
    }

    val ACTION_MANAGE_OVERLAY_PERMISSION: String? = onMinimumVersion(null){
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
    }

    fun isNotificationPolicyAccessGranted(notificationManager: NotificationManager): Boolean =
        onMinimumVersion(true) {
            notificationManager.isNotificationPolicyAccessGranted
        }



    private inline fun <T> onMinimumVersion(default: T, versionCode: Int = Build.VERSION_CODES
        .M, block: () -> T): T =
        if (Build.VERSION.SDK_INT >= versionCode) {
            block()
        } else {
            default
        }

}
