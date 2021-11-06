package com.example.tvguide.custom

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.logd

/**
 * Created by luyiling on 2020/9/23
 * Modified by
 *
 * TODO:
 * Description:
 * 因為寬度每個都不同, 無法使用layoutManager 去計算位置和移動的寬度
 * 也無法用smoothScrollBy 會使他不斷的被移動
 *
 * @func 單次捲動使用
 * @spec 卷軸方向端看LayoutManager, 可以卷軸的向量再記錄shift即可
 *
 * @用例 頻道欄 Vertical Scroll
 * @用例 節目表 Horizontal Scroll
 */
class Schedule @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    var offsetX = 0
    var offsetY = 0
    var offsetListener : OffsetScrollListener? = null

    //在移動的期間移動x量
    var shiftX = 0
    var shiftY = 0

    /*User主動才會invoke*/
    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        logd("FXXK recv state change")
        if(state == SCROLL_STATE_IDLE) {
            offsetListener?.onScrollIDEState(this, shiftX, shiftY)
            //歸零
            if(layoutManager?.canScrollVertically() == true) shiftY = 0
            if(layoutManager?.canScrollHorizontally() == true) shiftX = 0
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        super.scrollBy(x, y)
        logd("FXXK passive scroll")
    }

    /*被動/User主動控制 都會invoke*/
    override fun onScrolled(dx: Int, dy: Int) {
        offsetX += dx
        offsetY += dy
        shiftX += dx
        shiftY += dy
        logd("FXXK Nest Scroll: $offsetX, $dx")
        offsetListener?.onScrolled(dx, dy, offsetX, offsetY)

        /*被動*/
        if (scrollState == SCROLL_STATE_IDLE){
            //歸零
            if(layoutManager?.canScrollVertically() == true) shiftY = 0
            if(layoutManager?.canScrollHorizontally() == true) shiftX = 0
        }
    }

    interface OffsetScrollListener{
        fun onScrolled(dx: Int, dy: Int, accOffsetX : Int, accOffsetY :Int)
        /*主動*/
        fun onScrollIDEState(rv: RecyclerView, shiftX : Int, shiftY: Int)
    }
}