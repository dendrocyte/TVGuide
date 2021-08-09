package com.example.tvguide.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * Description:
 *
 * @params
 * @params
 */
data class TickGroupModel(val time : String, override val itemType : Int) : MultiItemEntity {

    companion object{
        const val START = 0
        const val OTHER = 1
    }
}

