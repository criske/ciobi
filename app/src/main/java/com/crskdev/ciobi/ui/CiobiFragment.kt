package com.crskdev.ciobi.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.graphics.toColorFilter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crskdev.ciobi.R
import com.crskdev.ciobi.domain.CiobiTaskID
import com.crskdev.ciobi.domain.CiobiTaskManagerProxy
import com.crskdev.ciobi.system.CiobiReportUIReceiver
import kotlinx.android.synthetic.main.ciobi_fragment.*
import kotlinx.android.synthetic.main.ciobi_report_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class CiobiFragment : Fragment() {

    private val viewModel: CiobiViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val context = context!!.applicationContext
                return CiobiViewModel(
                    DefaultDispatchers,
                    CiobiTaskManagerProxy(CiobiReportUIReceiver(context)),
                    StatusMapperImpl(context)
                ) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ciobi_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = CiobiReportsAdapter()
        with(listReports) {
            this.adapter = adapter
            setOnItemClickListener { _, _, position, _ ->
                val report = adapter.getItem(position)
                if(report.enabled){
                    viewModel.fix(report.id)
                }

            }
        }
        viewModel.liveDataReports.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })
    }

    private class CiobiReportsAdapter : BaseAdapter() {

        private val reports = mutableListOf<CiobiReportUI>()

        fun update(newReports: List<CiobiReportUI>) {
            reports.clear()
            reports.addAll(newReports)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, containter: ViewGroup): View =
            (convertView
                ?: LayoutInflater.from(containter.context)
                    .inflate(R.layout.ciobi_report_item, containter, false))
                .apply {
                    val report = getItem(position)
                    textName.text = report.name
                    textMessage.isVisible = report.message != null && report.message.isNotBlank()
                    textMessage.text = report.message
                    with(imageStatus) {
                        setImageResource(report.uiStatus.icon)
                        colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(report.uiStatus.color)
                    }
                    val taskIcon = when (report.id) {
                        CiobiTaskID.WIFI -> R.drawable.ic_wifi_black_24dp
                        CiobiTaskID.MobileData -> R.drawable.ic_signal_cellular_4_bar_black_24dp
                        CiobiTaskID.ScreenBrightness -> R.drawable.ic_brightness_7_black_24dp
                        CiobiTaskID.DownloadClutter -> R.drawable.ic_storage_black_24dp
                        CiobiTaskID.RecordClutter -> R.drawable.ic_play_circle_filled_black_24dp
                        CiobiTaskID.DnD -> R.drawable.ic_do_not_disturb_on_black_24dp
                        CiobiTaskID.Ringtone -> R.drawable.ic_ring_volume_black_24dp
                        CiobiTaskID.Bluetooth -> R.drawable.ic_bluetooth_black_24dp
                    }
                    imageTask.setImageResource(taskIcon)
                    imageTask.colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(Color.DKGRAY)
                }


        override fun getItem(position: Int): CiobiReportUI = reports[position]

        override fun getItemId(position: Int): Long = reports[position].id.ordinal.toLong()

        override fun getCount(): Int = reports.size

    }
}


