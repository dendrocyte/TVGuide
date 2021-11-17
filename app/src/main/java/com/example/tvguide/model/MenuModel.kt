package com.example.tvguide.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * Created by luyiling on 2021/11/12
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
data class MenuModel(
        val groupId : Int,
        val itemId : Int, //if is header = -1
        var isSelected : Boolean,
        val title: String,
        override val itemType: Int
        ) : MultiItemEntity{

            companion object{
                const val TYPE_HEADER = 0
                const val TYPE_DATA = 1
            }
        }