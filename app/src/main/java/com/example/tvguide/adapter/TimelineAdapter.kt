package com.example.tvguide.adapter

import android.graphics.drawable.ClipDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
import com.example.tvguide.custom.ScaleAgent
import com.example.tvguide.model.TickGroupModel

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class TimelineAdapter(data: List<TickGroupModel>)
    : BaseMultiItemQuickAdapter<TickGroupModel, BaseViewHolder>(data.toMutableList()) {

    init {
        addItemType(TickGroupModel.START1, R.layout.single_tick_start)
        addItemType(TickGroupModel.START2, R.layout.single_tick_start1)
        addItemType(TickGroupModel.END, R.layout.single_tick_end)
    }
    override fun convert(helper: BaseViewHolder, item: TickGroupModel) {
        //tune the width
        when(item.itemType){
            TickGroupModel.START1, TickGroupModel.START2 -> {
                val param = helper.itemView.layoutParams as ViewGroup.LayoutParams
                param.width = ScaleAgent.pxOf1HWidth.toInt()

                helper.getView<TextView>(R.id.tVtick).text = item.time
            }
            TickGroupModel.END -> {
                (helper.getView<ImageView>(R.id.iVpin).drawable as ClipDrawable).level = 5000
            }
        }

    }

}