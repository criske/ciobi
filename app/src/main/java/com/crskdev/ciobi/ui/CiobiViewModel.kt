package com.crskdev.ciobi.ui

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.*
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.domain.CiobiTaskManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Created by Cristian Pela on 09.02.2020.
 */
@ExperimentalCoroutinesApi
class CiobiViewModel(
    private val dispatchers: AbstractDispatchers,
    private val taskManager: CiobiTaskManager,
    private val statusMapper: StatusMapper
) : ViewModel() {

    val liveDataReports: LiveData<List<CiobiReportUI>> = liveData(timeoutInMs = 500) {
        taskManager
            .observeReports()
            .flowOn(dispatchers.Unconfined())
            .collect { list -> emit(list.map {
                it.toUI()
            }) }
    }

    fun fix(id: CiobiTaskID) {
        taskManager.execute(id)
    }

    private fun CiobiReport.toUI(): CiobiReportUI = CiobiReportUI(
        id = id,
        name = this.name,
        uiStatus = statusMapper.uiStatus(status),
        message = message,
        enabled = status == CiobiReport.Status.NEED_TO_FIX || status == CiobiReport.Status.ERROR
    )

}

object DefaultDispatchers : AbstractDispatchers {

    override fun IO(): CoroutineDispatcher = Dispatchers.IO

    override fun Main(): CoroutineDispatcher = Dispatchers.Main

    override fun Default(): CoroutineDispatcher = Dispatchers.Default

    override fun Unconfined(): CoroutineDispatcher = Dispatchers.Unconfined

}

interface AbstractDispatchers {
    fun IO(): CoroutineDispatcher
    fun Main(): CoroutineDispatcher
    fun Default(): CoroutineDispatcher
    fun Unconfined(): CoroutineDispatcher
}

data class CiobiReportUI(
    val id: CiobiTaskID,
    val name: String,
    val uiStatus: StatusMapper.StatusUI,
    val message: String?,
    val enabled: Boolean
)

interface StatusMapper {

    class StatusUI(val icon: Int, val color: Int)

    fun uiStatus(status: CiobiReport.Status): StatusUI

}

class StatusMapperImpl(private val context: Context) : StatusMapper {

    override fun uiStatus(status: CiobiReport.Status): StatusMapper.StatusUI =
        when (status) {
            CiobiReport.Status.PENDING -> {
                R.drawable.ic_help_black_24dp to Color.parseColor("#DCB931")
            }
            CiobiReport.Status.NEED_TO_FIX -> {
                R.drawable.ic_error_black_24dp to Color.parseColor("#C30915")
            }
            CiobiReport.Status.OK -> {
                R.drawable.ic_check_circle_black_24dp to Color.parseColor("#46D160")
            }
            CiobiReport.Status.ERROR -> {
                R.drawable.ic_error_black_24dp to Color.parseColor("#C30915")
            }
        }.let { StatusMapper.StatusUI(it.first, it.second) }

}