package com.example.tvguide.custom

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.logw

/**
 * Created by luyiling on 2020/10/7
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class Timeline @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var listener : ScrollListener? = null

    /**
     * do nothing
     */
    override fun addOnScrollListener(listener: OnScrollListener) {
        logw("Wrap by addScrollListener, not this function")
    }

    fun addStateScrollListener(listener : ScrollListener){
        super.addOnScrollListener(nativeListener)
        this.listener = listener
    }


    /*主動·被動都會被偵測到*/
    /*用來推算line marker indicator移動總量*/
    /** condition
     * 1.User主動控制 timeline
     * -> onScrolled(..) invoke 但此method始終不會是SCROLL_STATE_IDLE
     * -> onScrollStateChanged(..)
     * 2.被動控制timeline
     * -> 只有onScrolled(..) invoke 但state永遠是SCROLL_STATE_IDLE
     */
    private val nativeListener = object : OnScrollListener() {
        /*User主動控制/被動控制 timeline的view invoke this*/
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            listener?.onScrolled(recyclerView, dx, dy)

            if (recyclerView.scrollState == SCROLL_STATE_IDLE){
                /* 被動控制*/
                listener?.onPassiveScrollState(recyclerView, recyclerView.scrollState )
            }
        }
        /*被動控制 state並不會被改動 -> 不會invoke onScrollStateChanged(...)*/
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            listener?.onActiveScrollStateChanged(recyclerView, newState)
        }
    }

    interface ScrollListener{
        fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
        fun onActiveScrollStateChanged(recyclerView: RecyclerView, newState: Int)
        fun onPassiveScrollState(recyclerView: RecyclerView, state: Int)
    }
}