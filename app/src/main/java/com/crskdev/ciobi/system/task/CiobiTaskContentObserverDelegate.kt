package com.crskdev.ciobi.system.task

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar

/**
 * Created by Cristian Pela on 12.02.2020.
 */
class CiobiTaskContentObserverDelegate(handler: Handler): ContentObserver(handler), CiobiSystemObserverRegistrar {

    val handler = handler

    private val observers  = mutableMapOf<Uri, CiobiSystemObserverRegistrar.Delegated>()

    override fun onChange(selfChange: Boolean, uri: Uri) {
        observers[uri]?.onChange()
    }

    override fun register(context: Context) {
        context.contentResolver
            .registerContentObserver(Settings.System.CONTENT_URI, true, this)
    }

    override fun unregister(context: Context) {
        context.contentResolver
            .unregisterContentObserver(this)
        observers.clear()
    }

    fun registerDelegated(uri: Uri, observer: CiobiSystemObserverRegistrar.Delegated){
        observers[uri] = observer
    }

    fun unregisterDelegated(uri: Uri){
        observers.remove(uri)
    }

}