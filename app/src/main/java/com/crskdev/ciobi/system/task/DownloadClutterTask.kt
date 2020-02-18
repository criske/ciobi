package com.crskdev.ciobi.system.task

import android.Manifest
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.text.format.Formatter
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiReport
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.system.CiobiSystemObserverRegistrar
import com.crskdev.ciobi.system.sendBroadcastStatus
import com.crskdev.ciobi.system.util.isGranted
import java.io.File

/**
 * Created by Cristian Pela on 14.02.2020.
 */
object DownloadClutterTask {

    fun check(context: Context) {
        var report = CiobiReport(
            CiobiTaskID.DownloadClutter,
            context.getString(R.string.download_clutter_name)
        )

        val hasPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE.isGranted(context)
        if (!hasPermission) {
            context.sendBroadcastStatus(
                report.copy(
                    status = CiobiReport.Status.ERROR,
                    message = context.getString(R.string.redirect_error_permission_denied),
                    redirectIntent = ".MainActivity"
                )
            )
        } else {
            val downloads = getDownloadsDir(context)
            if (downloads == null) {
                report = report.copy(
                    message = context.getString(
                        R.string.download_folder_not_found_error
                    )
                )
            } else {
                val htmlFilesSequence = htmlFilesSequence(downloads)
                val htmlFileSize = htmlFilesSequence.fold(0L) { acc, curr ->
                    acc + curr.length()
                }
                report = if (htmlFileSize > 0) {
                    report.copy(
                        status = CiobiReport.Status.NEED_TO_FIX,
                        message = context.getString(
                            R.string.download_clutter_value,
                            htmlFileSize.lengthToString(context)
                        )
                    )
                } else {
                    report.copy(status = CiobiReport.Status.OK)
                }
            }
            context.sendBroadcastStatus(report)
        }
    }

    fun fix(context: Context) {
        val default = CiobiReport(
            CiobiTaskID.DownloadClutter,
            context.getString(R.string.download_clutter_name)
        )

        val hasPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE.isGranted(context)
        if (!hasPermission) {
            context.sendBroadcastStatus(
                default.copy(
                    status = CiobiReport.Status.ERROR,
                    message = context.getString(R.string.redirect_error_permission_denied),
                    redirectIntent = ".MainActivity"
                )
            )
            return
        }

        val downloads = getDownloadsDir(context)
        if (downloads != null) {
            htmlFilesSequence(downloads).toList().forEach {
                it.delete()
            }
            context.sendBroadcastStatus(default.copy(status = CiobiReport.Status.OK))
        }

    }

    private fun getDownloadsDir(context: Context) =
//        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    private fun htmlFilesSequence(directory: File): Sequence<File> = directory
        .walkTopDown()
        .filter {
            it.extension == "htm" || it.extension == "html" || it.extension == "mhtml"
        }


    private fun Long.lengthToString(context: Context): String =
        Formatter.formatFileSize(context, this)


//    class DownloadClutterReceiver2(private val context: Context, handler: Handler) :
//        ContentObserver(handler),
//        CiobiSystemObserverRegistrar {
//
//        override fun onChange(selfChange: Boolean, uri: Uri?) {
//            check(context)
//        }
//
//        override fun register(context: Context) {
//            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                MediaStore.Downloads.EXTERNAL_CONTENT_URI
//            else
//                MediaStore.Files.getContentUri("external")
//            context.contentResolver.registerContentObserver(uri, true, this)
//        }
//
//        override fun unregister(context: Context) {
//            context.contentResolver.unregisterContentObserver(this)
//        }
//
//    }

    class DownloadClutterReceiver(private val context: Context) : FileObserver(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    ), CiobiSystemObserverRegistrar {

        override fun onEvent(event: Int, path: String?) {
            if (event == CREATE) {
                check(context)
            }
        }

        override fun register(context: Context) {
            startWatching()
            check(context)
        }

        override fun unregister(context: Context) {
            stopWatching()
        }

    }

}