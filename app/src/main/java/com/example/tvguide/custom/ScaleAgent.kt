package com.example.tvguide.custom

import android.content.res.Resources

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
object ScaleAgent {
    /**
     * NOTE 要改UI版規格 要設定ratioOf1HPerScreen
     * 設定死規格
     */
    const val ratioOfUIMyDesign = 0.29f
    private var ratioOf1HPerScreen : Float = ratioOfUIMyDesign

    /**
     * @功用 被動索取
     */
    var pxOf1HWidth : Float = 0f
        get() = Resources.getSystem().displayMetrics.widthPixels * ratioOf1HPerScreen

}

