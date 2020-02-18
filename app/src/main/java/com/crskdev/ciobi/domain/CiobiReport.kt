package com.crskdev.ciobi.domain

/**
 * Created by Cristian Pela on 09.02.2020.
 */
data class CiobiReport(
    val id: CiobiTaskID,
    val name: String,
    val status: Status = Status.PENDING,
    val message: String? = null,
    val redirectIntent: String? = null,
    val data: Any? = null) {

    enum class Status{ PENDING, NEED_TO_FIX, OK, ERROR}

}



