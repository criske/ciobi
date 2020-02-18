package com.crskdev.ciobi.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

interface CiobiTaskManager {
    fun observeReports(): Flow<List<CiobiReport>>
    fun execute(taskID: CiobiTaskID)
}

@ExperimentalCoroutinesApi
class CiobiTaskManagerProxy(private vararg val managers: CiobiTaskManager) : CiobiTaskManager {

    override fun observeReports(): Flow<List<CiobiReport>> =
        managers.map { it.observeReports() }.merge()

    override fun execute(taskID: CiobiTaskID) {
        managers.forEach { it.execute(taskID) }
    }

}