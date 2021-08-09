package com.example.tvguide.custom

import android.view.MotionEvent
import android.view.View

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @param defaultNewDx 初始值 timeline起點與line marker的距離
 * @param
 */
class LineMarkerAgent(var defaultNewDx : Float) {
    var xStart: Float = 0f //移動範圍的限制
    set(value) {
        range = value.rangeTo(xEnd)
        field = value
    }
    var xEnd : Float = 0f //移動範圍的限制
    set(value){
        range = xStart.rangeTo(value)
        field = value
    }
    //indicator 在timeline上移動的總量
    //讓數值可以靜態被擷取
    var newdX : Float = defaultNewDx
    private set(value){
        newXListener?.invoke(value)
        field = value
    }
    private lateinit var range : ClosedFloatingPointRange<Float>
    //動態獲得 indicator 在timeline上移動的總量
    var newXListener : ((Float)-> Unit)? = null





    var dX : Float = 0f
    /*could drag smoothly-> x, y*/
    val touchListener = View.OnTouchListener { v, event ->
        when (event.action) {

            MotionEvent.ACTION_DOWN -> dX = v.x - event.rawX //作為歸零的動作
            MotionEvent.ACTION_MOVE -> {
                v.animate() //make it shift smoothly
                    .x((event.rawX + dX).run {
                        when {
                            range.contains(this) -> this
                            this.compareTo(range.start) > 0 -> range.endInclusive
                            else -> range.start
                        }
                    })
                    .setDuration(0)
                    .start()
            }

            MotionEvent.ACTION_UP -> newdX = v.x -xStart //傳出去根據timeline推算duration
            else -> return@OnTouchListener false
        }
        return@OnTouchListener true
    }


}