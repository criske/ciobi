package com.crskdev.ciobi.system

import android.content.Context

/**
 * Created by Cristian Pela on 12.02.2020.
 */
interface CiobiSystemObserverRegistrar{
    fun register(context: Context)
    fun unregister(context: Context)
    interface Delegated {
        fun onChange()
    }
}
