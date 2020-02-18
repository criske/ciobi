package com.crskdev.ciobi.system.task

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus
import com.crskdev.ciobi.system.util.SettingsSystemCompat

/**
 * Created by Cristian Pela on 12.02.2020.
 */
object NetworkTask {

    fun fixWifi(context: Context) {
        val wifiManager = context.getSystemService<WifiManager>()!!
        val success = wifiManager.setWifiEnabled(true)
        if (!success) {
            context.sendBroadcastStatus(
                CiobiReport(
                    id = CiobiTaskID.WIFI,
                    name = "WiFi",
                    status = CiobiReport.Status.ERROR,
                    message = context.getString(
                        R.string.redirect_error_unsupported
                    ),
                    redirectIntent = SettingsSystemCompat.ACTION_MANAGE_WRITE_SETTINGS
                )
            )
        }
    }

    fun fixMobileData(context: Context) {
        context.sendBroadcastStatus(
            CiobiReport(
                id = CiobiTaskID.MobileData,
                name = context.getString(R.string.mobile_data_name),
                status = CiobiReport.Status.ERROR,
                message = context.getString(
                    R.string.redirect_error_unsupported
                )
            )
        )
        context.startActivity(
            Intent().setComponent(
                ComponentName(
                    "com.android.phone",
                    "com.android.phone.MobileNetworkSettings"
                )
            )
        )
    }

    fun check(context: Context) {
        val wifiManager = context.getSystemService<WifiManager>()!!
        val wifiEnabled = wifiManager.isWifiEnabled
        context.sendBroadcastStatus(
            CiobiReport(
                CiobiTaskID.WIFI, "WiFi",
                if (wifiEnabled) CiobiReport.Status.OK else CiobiReport.Status
                    .NEED_TO_FIX,
                if(wifiEnabled) null else context.getString(R.string.wifi_value)
            )
        )
        val phoneManager = context.getSystemService<TelephonyManager>()!!
        val isMobileDataEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            phoneManager.isDataEnabled
        } else {
            phoneManager.simState == TelephonyManager.SIM_STATE_READY
                    && phoneManager.dataState != TelephonyManager.DATA_DISCONNECTED;
        }
        context.sendBroadcastStatus(
            CiobiReport(
                CiobiTaskID.MobileData, context.getString(R.string.mobile_data_name),
                if (isMobileDataEnabled) CiobiReport.Status.OK else CiobiReport.Status.NEED_TO_FIX,
                if(isMobileDataEnabled) null else context.getString(R.string.mobile_data_value)
            )
        )
    }

    class NetworkObserver(private val context: Context) : ConnectivityManager.NetworkCallback(),
        CiobiSystemObserverRegistrar {

        private val manager = context.getSystemService<ConnectivityManager>()!!

        private val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()


        override fun register(context: Context) {
            manager.registerNetworkCallback(networkRequest, this)
            check(context)
        }

        override fun unregister(context: Context) {
            manager.unregisterNetworkCallback(this)
        }

        override fun onLost(network: Network) {
            check(context)
        }

        override fun onAvailable(network: Network) {
            check(context)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            check(context)
        }
    }

}