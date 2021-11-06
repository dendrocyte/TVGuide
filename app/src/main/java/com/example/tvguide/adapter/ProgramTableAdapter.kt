package com.example.tvguide.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
import com.example.tvguide.custom.Schedule
import com.example.tvguide.logd
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

    var passiveHScrollSubject : PublishSubject<Pair<Int, Int>>? = null

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
                    //FIXME
//                        startActivity(intentFor<PlayerActivity>(
//                            ARG_VIDEO to this.data[position].playItem,
//                            ARG_ANAL to this.data[position].botAnalyst
//                        ))
                }

            }

            //因為每一個的schedule recyclerview的id都一樣，要用tag 做區分
            this@with.tag = item.key.hashCode()

            //因為embedded recyclerview
            // (如: 父recyclerview + 子recyclerview) (如：父 nestScrollView + 子recyclerview)
            // 子recyclerview 都會沒收到ScrollListener, 需做客製化
            offsetListener = object : Schedule.OffsetScrollListener{
                override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                    //主動被動都會invoke
                }
                override fun onScrollIDEState(rv: RecyclerView, shiftX: Int, shiftY: Int) {
                    passiveHScrollSubject?.onNext(this@with.tag as Int to shiftX)
                }
            }
            passiveHScrollSubject?.subscribe {
                logd("schedule: recv passive scroll...")
                //不可以用smoothScrollBy 移動的數值會不對
                if (it.first != this@with.tag as Int) this@with.scrollBy(it.second,0)
            }
        }


    }


}
