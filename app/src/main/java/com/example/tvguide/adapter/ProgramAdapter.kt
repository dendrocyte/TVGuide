package com.example.tvguide.adapter

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
import com.example.tvguide.custom.ScaleAgent
import com.example.tvguide.logd
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.toHHmm

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * TODO:若還是無法解決資料會亂load的問題再考慮做group
 * PATCH: 改變Model 的架構，從github 裡拿
 * Description:
 *
 * @params
 * @params
 */
class ProgramAdapter(data : List<TVScheduleModel>)
    : BaseQuickAdapter<TVScheduleModel, BaseViewHolder>(R.layout.single_live_program, data.toMutableList()) {

    //讓節目時間間隔小於200，就不顯示 "No Program"
    val THRESHOLD_NO_PG_TXT_SHOW = 150

    init {
        /*addChildClickViewIds 不能放在convert()*/
        addChildClickViewIds(R.id.iVmask)
    }
    override fun convert(helper: BaseViewHolder, item: TVScheduleModel) {
        //EpisodeMilliSecond: 60*60*1000
        val hDuration = (item.scheduleEnd-item.scheduleStart).toFloat()/(60*60*1000)
        logd("duration: $hDuration")
        val param = helper.itemView.layoutParams as ViewGroup.LayoutParams
        param.width = (ScaleAgent.pxOf1HWidth * hDuration).toInt()
        logd("width: ${param.width}")

        helper.getView<TextView>(R.id.tVmsg).visibility =
            if(item.program != null) View.GONE
            else if (param.width <= THRESHOLD_NO_PG_TXT_SHOW) View.GONE
            else View.VISIBLE

        helper.getView<ImageView>(R.id.iVthumbnail).apply {
            alpha = if (item.program == null) 0.6f else 1f
            Glide.with(context)
                .load(item.program?.thumbnail)
                .into(this)
        }

        helper.getView<ImageView>(R.id.iVmask).apply {
            setBackgroundColor(
                if (item.program == null) Color.TRANSPARENT
                else ContextCompat.getColor(context, R.color.mask)
            )
        }

        helper.getView<TextView>(R.id.tVtitle).text =
                if (item.program == null) ""
                else
                    "${item.scheduleStart.toHHmm}-${item.scheduleEnd.toHHmm}\n${item.program?.title}"
    }

    /*複寫，以做到不同view type 做有無註冊listener的區分*/
    override fun setOnItemChildClick(v: View, position: Int) {
        if (data[position].program != null && v.id == R.id.iVthumbnail)
            super.setOnItemChildClick(v, position)
    }
}