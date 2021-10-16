package com.example.tvguide.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R

/**
 * Created by luyiling on 2021/10/16
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class ChannelAdapter(list: List<String>)
    : BaseQuickAdapter<String, BaseViewHolder>(R.layout.single_channel, list.toMutableList()) {

    override fun convert(holder: BaseViewHolder, item: String) {
        holder.getView<TextView>(R.id.tVChannel).text = item
    }
}