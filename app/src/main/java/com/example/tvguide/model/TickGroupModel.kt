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

        /** Design 1
         * the first one is START
         * the rest is OTHER
         *
         //const val START = 0
         //const val OTHER = 1
         */


        /** Design 2
         * the odd is START1
         * the even is START2
         */
        const val START1 = -2
        const val START2 = -3
    }
}

