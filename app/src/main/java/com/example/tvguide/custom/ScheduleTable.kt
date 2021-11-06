package com.example.tvguide.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.widget.NestedScrollView
import com.example.tvguide.logd

/**
 * Created by luyiling on 2021/11/6
 * Modified by
 *
 * TODO:
 * Description: 因為scrollChange() 在主動被動都會觸發，要分離出主動/被動
 *
 * @params
 * @用例 總體節目表 Vetical Scroll
 */
class ScheduleTable @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    //暫存主動(0)/被動(1) flag
    @NestedScrollType var scrollType : Int = 0

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        logd("Start NestSV: $type")
        scrollType = type
        return super.startNestedScroll(axes, type)
    }

    override fun stopNestedScroll(type: Int) {
        logd("Stop NestSV: $type")
        scrollType = type
        super.stopNestedScroll(type)
    }

    override fun scrollBy(x: Int, y: Int) {
        logd("NestSV Stroll by...")
        scrollType = ViewCompat.TYPE_NON_TOUCH
        super.scrollBy(x, y)
    }
}