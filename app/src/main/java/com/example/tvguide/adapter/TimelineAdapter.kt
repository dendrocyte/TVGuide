package com.example.tvguide.adapter

import android.view.ViewGroup
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
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
        addItemType(TickGroupModel.START, R.layout.single_tick_start)//PATCH: change
        addItemType(TickGroupModel.OTHER, R.layout.single_tick)//PATCH: change
    }
    override fun convert(helper: BaseViewHolder, item: TickGroupModel) {
        //tune the width
        //FIXME 未來在這裡做不同尺寸的機體辨別
        val param = helper.itemView.layoutParams as ViewGroup.LayoutParams
        param.width = (context.resources.displayMetrics.widthPixels * 0.65).toInt()//PATCH: change

        helper.getView<TextView>(R.id.tVtick).text = item.time
    }

}