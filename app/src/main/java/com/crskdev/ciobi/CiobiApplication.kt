package com.crskdev.ciobi

import android.app.Application
import com.crskdev.ciobi.system.CiobiWatchingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Created by Cristian Pela on 11.02.2020.
 */
@ExperimentalCoroutinesApi
@FlowPreview
class CiobiApplication : Application() {

    override fun onCreate() {
        CiobiWatchingService.startInForeground(this)
        super.onCreate()
    }
}