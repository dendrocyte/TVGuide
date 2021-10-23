package com.example.tvguide.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
            else View.VISIBLE

        helper.getView<ImageView>(R.id.iVthumbnail).apply {
            Glide.with(context)
                .load(item.program?.thumbnail)
                .into(this)
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