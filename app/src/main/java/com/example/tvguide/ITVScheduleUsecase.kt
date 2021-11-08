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
                -> when{
                    index == 24 -> TickGroupModel.END
                    index % 2 == 0 -> TickGroupModel.START1
                    else -> TickGroupModel.START2
                }
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
        loop@ for (k in hashMap.keys) {
            val list = mutableListOf<TVScheduleModel>()
            inner@ for ((index, liveScheduleModel) in hashMap[k]!!.withIndex()){
                println("channelName: $k, index:$index, ${hashMap[k]!!.size}")
                when{
                    //資料過期
                    liveScheduleModel.scheduleEnd < currentDateStart -> {
                        println("資料都過期了唷")
                        //不做其他事, 交由下一節目處理
                    }
                    //節目有跨過晚上12H
                    liveScheduleModel.scheduleStart < currentDateStart -> {
                        println("資料超過午夜12H唷")
                        liveScheduleModel.scheduleStart = currentDateStart
                        list.add(liveScheduleModel)
                    }
                    else -> {
                        println("正常資料")

                        if (index == 0){
                            //++++++++++++++ 第一個資料 +++++++++++++++++//

                            //No content available frame
                            /*val ob = LiveScheduleModel(
                                liveScheduleModel.channelName,
                                currentDateStart,
                                currentDateEnd,
                                null,
                                null,
                                null
                            )*/

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

                        } else {
                            //++++++++++++++ no.2...n資料 +++++++++++++++++//

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
                        //資料過期
                        liveScheduleModel.scheduleEnd < currentDateStart ->{
                            list.addAll(create1HEmptyGaps(
                                currentDateStart, currentDateEnd, liveScheduleModel.channelName
                            ))
                        }
                        newEnd > currentDateEnd -> {
                            list.last().scheduleEnd = currentDateEnd
                        }
                        else ->{
                            //之前已經加過資料了
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
        //特殊情境：start 和 end 相同 要排除
        if (start == end) return emptyList()

        val h = 60 * 60 * 1000
        //start之後的下一個小時點
        val headH = ((start - currentDateStart) / h) + 1
        //end之前的最近一個小時點
        val endH = (end - currentDateStart) / h


        val times = (endH - headH).toInt()
        val hasReminders = ((end - currentDateStart) % h) != (0).toLong()
        logd("create 1H empty gaps : $start, $end, $headH, $endH, $times, $hasReminders, $channelName")
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

        ///////////////////////// 添加頭中部 /////////////////////////////////////

        /**
         * @情況2: start-end 超過規格時間（1H/3H）
         */
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
            if (!hasReminders) return list
        }

        /**
         * @情況1: start-end 不到(1H/3H)
         * @情況2: start-end 超過規格時間（1H/3H）＋start是整點
         * @情況3: start-end 剛好1H/3H
         *
         * @用例 start非整, end 非整 [1H] [3H(含)內整點]
         * @用例 start非整, end 整 [1H內] [3H內]
         * @用例 start整, end 非整 [1H多] [3H多]
         * @用例 start整, end 整   [1H整] [3H整]
         */
        if (times == 0){
            //添加head
            val ob1 = ob.copy(
                    channelName = ob.channelName,
                    scheduleStart = start,
                    scheduleEnd = currentDateStart + headH * h
            )
            list.add(ob1)
            loge("HEAD: final create gaps: $ob1")
            if (!hasReminders) return list
        }

        /**
         * 說明
         * @情況1: start-end 不到(1H/3H)
         *
         * Scale_1H：
         * @about times <0 代表start跟end的間距不到 (1H/3H) + start和end 的小時是相同的
         * @用例 start非整, end 非整 [1H內]
         * @用例 start整, end 非整   [1H內]
         * 故Scale_1H只需要添加end 即可
         *
         * Scale_3H：
         * @用例 start非整, end 非整                    [3H內]
         * @用例 start非整, end 整 + start的時分 為3H倍數 [2H內]
         * @用例 start整, end 非整                      [3H內]
         * @用例 start整, end 整                        [3H內整點(不含3H整點)]
         * 故Scale_3H 就需要做添加特殊end
         */

        ///////////////////////// 添加尾部 /////////////////////////////////////

        /**
         * 添加 end
         * @about 只要end 非整即會至此
         *
         * @情況1: start-end 不到(1H/3H)
         * @情況2: start-end 超過(1H/3H)
         */
        if (hasReminders) {
            val ob1 =
                    if (times < 0) ob.copy(
                            channelName = ob.channelName,
                            scheduleStart = start,
                            scheduleEnd = end
                    )
                    else ob.copy(
                            channelName = ob.channelName,
                            scheduleStart = currentDateStart + endH * h,
                            scheduleEnd = end
                    )

            list.add(ob1)
            loge("END: final create gaps: $ob1")
            return list
        }
        return list
    }






    //***************** 每一次usecase 都要重啟計算一次 ******************//
    private val currentDateStart : Long = todayStart

    private val currentDateEnd : Long = today24HEnd

}