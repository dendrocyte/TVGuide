package com.example.tvguide.adapter

import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
import com.example.tvguide.custom.Schedule
import com.example.tvguide.logd
import com.example.tvguide.model.Analyst
import com.example.tvguide.model.PlayItem
import com.example.tvguide.model.TVScheduleModel
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Created by luyiling on 2021/11/6
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class ProgramTableAdapter(hashMap : Map<String, List<TVScheduleModel>>)
    : BaseQuickAdapter<Map.Entry<String, List<TVScheduleModel>>, BaseViewHolder>(
     R.layout.single_live_schedule_my_design, hashMap.entries.toMutableList()
    ) {
    /**
     * feed by activity or frag
     */
    var passiveHScrollSubject : PublishSubject<Pair<Int, Int>>? = null
    var naviPgListener : ((PlayItem, Analyst) -> Unit)? = null

    override fun convert(holder: BaseViewHolder, item: Map.Entry<String, List<TVScheduleModel>>) {

        with(holder.getView<Schedule>(R.id.recyclerProgram)){
            logd("child schedule id: ${this.id}")
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = ProgramAdapter(item.value).apply {
                setOnItemChildClickListener { adapter, view, position ->
                    logd("$view:Thumbnail click---$position")

                    checkNotNull(this.data[position].playItem)
                    checkNotNull(this.data[position].botAnalyst)
                    naviPgListener?.invoke(
                        this.data[position].playItem!!,
                        this.data[position].botAnalyst!!
                    )
                }

            }

            //??????????????????schedule recyclerview???id??????????????????tag ?????????
            this@with.tag = item.key.hashCode()

            //??????embedded recyclerview
            // (???: ???recyclerview + ???recyclerview) (????????? nestScrollView + ???recyclerview)
            // ???recyclerview ???????????????ScrollListener, ???????????????
            offsetListener = object : Schedule.OffsetScrollListener{
                override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                    //??????????????????invoke
                }
                override fun onScrollIDEState(rv: RecyclerView, shiftX: Int, shiftY: Int) {
                    passiveHScrollSubject?.onNext(this@with.tag as Int to shiftX)
                }
            }
            passiveHScrollSubject?.subscribe {
                logd("schedule: recv passive scroll...")
                //????????????smoothScrollBy ????????????????????????
                if (it.first != this@with.tag as Int) this@with.scrollBy(it.second,0)
            }
        }


    }


}
