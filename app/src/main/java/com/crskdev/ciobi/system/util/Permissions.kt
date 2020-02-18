package com.crskdev.ciobi.system.util

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/**
 * Created by Cristian Pela on 12.02.2020.
 */
fun checkDeniedPermissions(context: Context): Array<String> {
    val permissions = mutableListOf<String>()
    if (!Manifest.permission.WRITE_EXTERNAL_STORAGE.isGranted(context)) {
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    if (!Manifest.permission.READ_EXTERNAL_STORAGE.isGranted(context)) {
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (!Manifest.permission.GET_ACCOUNTS.isGranted(context)) {
        permissions.add(Manifest.permission.GET_ACCOUNTS)
    }
    return permissions.toTypedArray()
}

fun String.isGranted(context: Context) =
    ContextCompat.checkSelfPermission(context, this) == PackageManager.PERMISSION_GRANTED

fun Context.getAccountName(): String? = null
    //todo Android.Accounts.AccountManager.NewChooseAccountIntent(null,null,null,null,null,null,null)
//    return if (Manifest.permission.GET_ACCOUNTS.isGranted(this)) {
////        val accountManger = getSystemService<AccountManager>()!!
////        accountManger.accounts.firstOrNull()?.name
////    } else {
////        null
////    }

