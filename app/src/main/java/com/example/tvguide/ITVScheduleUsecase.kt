package com.example.tvguide

import com.example.tvguide.*
import com.example.tvguide.custom.TickDesign
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.model.TickGroupModel
import kotlin.collections.HashMap

/**
 * Created by luyiling on 2020/9/18
 * Modified by
 *
 * TODO:
 * Description: 由rx/, coroutine/ 實作
 *
 */
abstract class ITVScheduleUsecase <out T>(timelineDesignFlag : TickDesign) {

    //FIXME: 是否會從server?
    val filterMenuList = listOf("News", "Sports", "All")


    /** live schedule
     * NOTE change the timeline UI type here
     */
    val timeline = (0..24).mapIndexed { index, i ->
        TickGroupModel(
            String.format("%02d:00", i),
            when(timelineDesignFlag){
                TickDesign.BOSS
                -> if (index == 0 ) TickGroupModel.START else TickGroupModel.OTHER
                TickDesign.DEBOUNCE
                -> if (index % 2 == 0) TickGroupModel.START1 else TickGroupModel.START2
                TickDesign.DEFAULT
                -> TickGroupModel.START1
            }
        )
    }

    abstract fun requestSchedule() : T


    //************************ Gap 計算 ****************************//

    //限制gap的長度且多個gap段 才不會覺得太長都沒資料！
    protected fun fillGap(hashMap: HashMap<String, List<TVScheduleModel>>)
            : HashMap<String, List<TVScheduleModel>>{
        for (k in hashMap.keys) {
            val list = mutableListOf<TVScheduleModel>()
            hashMap[k]!!.forEachIndexed { index, liveScheduleModel ->
                println("channelName: $k, index:$index, ${hashMap[k]!!.size}")
                //++++++++++++++ 第一個資料 +++++++++++++++++//
                if (index == 0) {
                    //No content available frame
                    /*val ob = LiveScheduleModel(
                        liveScheduleModel.channelName,
                        currentDateStart,
                        currentDateEnd,
                        null,
                        null,
                        null
                    )*/
                    when{
                        //資料過期
                        liveScheduleModel.scheduleEnd < currentDateStart -> {
                            //創建 gap (1H 為單位)
                            list.addAll(create1HEmptyGaps(currentDateStart, currentDateEnd, liveScheduleModel.channelName))
                            //創建一整段的gap
                            //list.add(ob)
                            return@forEachIndexed
                        }
                        //節目有跨過晚上12H
                        liveScheduleModel.scheduleStart < currentDateStart -> {
                            liveScheduleModel.scheduleStart = currentDateStart
                            list.add(liveScheduleModel)
                        }
                        else -> {
                            //創建 gap (1H 為單位)
                            list.addAll(create1HEmptyGaps(
                                currentDateStart,
                                liveScheduleModel.scheduleStart,
                                liveScheduleModel.channelName
                            ))
                            //創建一整段的gap
                            //ob.scheduleEnd = liveScheduleModel.scheduleStart
                            //list.add(ob)
                            list.add(liveScheduleModel)
                        }
                    }
                }

                //++++++++++++++ no.2...n資料 +++++++++++++++++//
                if (index != 0) {//hashMap[k]!!.lastIndex
                    println("no.2..n: $index, ${hashMap[k]!!.lastIndex}")
                    val newStart = liveScheduleModel.scheduleStart
                    val lastEnd = hashMap[k]!![index - 1].scheduleEnd
                    if (newStart != lastEnd) {
                        //No content available frame
                        /*val ob = LiveScheduleModel(
                            liveScheduleModel.channelName,
                            lastEnd,
                            newStart,
                            null,
                            null,
                            null
                        )*/

                        //創建 gap (1H 為單位)
                        list.addAll(create1HEmptyGaps(lastEnd, newStart, liveScheduleModel.channelName))
                        //創建一整段的gap
                        //list.add(ob)
                        list.add(liveScheduleModel)
                    } else {
                        list.add(liveScheduleModel)
                    }
                }
                //++++++++++++++ 最後一個資料 +++++++++++++++++//
                if (index == hashMap[k]!!.lastIndex) {
                    val newEnd = liveScheduleModel.scheduleEnd
                    //No content available frame
                    /*val ob = LiveScheduleModel(
                        liveScheduleModel.channelName,
                        newEnd,
                        currentDateEnd,
                        null,
                        null,
                        null
                    )*/
                    when{
                        hashMap[k]!!.size == 1 ->{//代表已做完index = 0
                            //創建 gap (1H 為單位)
                            list.addAll(create1HEmptyGaps(
                                newEnd, currentDateEnd, liveScheduleModel.channelName
                            ))
                            //創建一整段的gap
                            //list.add(ob)
                        }
                        newEnd > currentDateEnd -> {
                            liveScheduleModel.scheduleEnd = currentDateEnd
                            list.add(liveScheduleModel)
                        }
                        else ->{
                            list.add(liveScheduleModel)
                            //創建 gap (1H 為單位)
                            list.addAll(create1HEmptyGaps(
                                newEnd, currentDateEnd, liveScheduleModel.channelName
                            ))
                            //創建一整段的gap
                            //list.add(ob)
                        }
                    }
                }
            }
            //舊的<k,list> 會被取代
            println("=======================")
            hashMap[k] = list
        }
        return hashMap
    }


    protected fun create1HEmptyGaps(start: Long, end: Long, channelName : String) : List<TVScheduleModel>{
        val h = 60 * 60 * 1000
        //start之後的下一個小時點
        val headH = ((start - currentDateStart) / h) + 1
        //end之前的最近一個小時點
        val endH = (end - currentDateStart) / h


        val times = (endH - headH).toInt()
        val hasReminders = ((end - currentDateStart) % h) != (0).toLong()
        logd("create 1H empty gaps : $start, $end, $headH, $endH, $times, $hasReminders")
        var list = mutableListOf<TVScheduleModel>()

        //No content available frame
        //template
        val ob = TVScheduleModel(
            channelName,
            0,
            0,
            null,
            null,
            null
        )

        //times <0 代表start跟end的間距不到 1H
        //情況2: start-end 超過1H
        if (times > 0){
            //添加head
            val ob1 = ob.copy(
                channelName = ob.channelName,
                scheduleStart = start,
                scheduleEnd = currentDateStart + headH * h
            )
            list.add(ob1)
            //正式來時不要打印 非常耗內存
            //loge("HEAD: final create gaps: $ob1")

            //中間間隔
            for (i in 0 until times){
                val ob1 = ob.copy(
                    channelName = ob.channelName,
                    scheduleStart = currentDateStart + headH * h + h * i,
                    scheduleEnd = currentDateStart + headH * h + h * (i+1)
                )
                list.add(ob1)
                //正式來時不要打印 非常耗內存
                //loge("MIDDLE: final create gaps: $ob1")
            }
        }

        //情況1: start-end 不到1H
        //情況2: start-end 超過1H
        //添加 end
        if (hasReminders) {
            val ob1 = ob.copy(
                channelName = ob.channelName,
                scheduleStart = currentDateStart + endH * h,
                scheduleEnd = end
            )
            list.add(ob1)
            //正式來時不要打印 非常耗內存
            //loge("END: final create gaps: $ob1")
        }
        return list
    }






    //***************** 每一次usecase 都要重啟計算一次 ******************//
    private val currentDateStart : Long = todayStart

    private val currentDateEnd : Long = today25HEnd

}