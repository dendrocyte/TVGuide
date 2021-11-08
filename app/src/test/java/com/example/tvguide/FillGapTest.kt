package com.example.tvguide

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.tvguide.model.TVScheduleModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.time.LocalDate

/**
 * Created by luyiling on 2021/11/8
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class FillGapTest {
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    lateinit var usecase : ITVScheduleUsecase<*>
    lateinit var method : Method
    lateinit var fieldStart : Field
    lateinit var fieldEnd : Field
    lateinit var today : LocalDate
    lateinit var yesterday : LocalDate
    lateinit var chanName : String
    lateinit var fakeOb : TVScheduleModel
    lateinit var fakeObs : List<TVScheduleModel>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        usecase = object : ITVScheduleUsecase<Any>(mockk(relaxed = true)){
            override fun requestSchedule(): Any {
                return println("This is Mock Test")
            }
        }
        method = ITVScheduleUsecase::class.java.getDeclaredMethod(
            "fillGap", HashMap::class.java
        )
        method.isAccessible = true

        //因為之後都是以 LocalDate 做時間注入 creatEmptyGaps 做邏輯探討
        //LocalDate是 No Zone Offset
        //只要把currentDateStart 更改為timeStartForNoZoneOffset就一致了
        fieldStart = ITVScheduleUsecase::class.java.getDeclaredField("currentDateStart")
        fieldStart.isAccessible = true
        fieldEnd = ITVScheduleUsecase::class.java.getDeclaredField("currentDateEnd")
        fieldEnd.isAccessible = true

        chanName = "Test Channel"
        fakeOb = TVScheduleModel(
            chanName,
            0,
            0,
            null,
            null,
            null
        )
        fakeObs = listOf(
            fakeOb.copy(),
            fakeOb.copy()
        )
        today = LocalDate.now()
        yesterday = LocalDate.now().minusDays(1)

    }

    /**
     * 只有一個節目
     * 這節目是昨天的
     */
    @Test
    fun fillGap_max1_lastPG(){
        val start : Long = yesterday.atTime(12, 0).toEpochMills
        val end : Long = yesterday.atTime(13, 0).toEpochMills
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(fakeOb.copy(
            scheduleStart = start, scheduleEnd = end
        )))

        //創造出expMap
        val list = mutableListOf<TVScheduleModel>()
        var hStart : Long = todayStartForNoZoneOffset
        while (hStart < todayEndForNoZoneOffset){
            list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }

    /**
     * 只有一個節目
     * 節目是跨24H
     */
    @Test
    fun fillGap_max1_overnight(){
        val start : Long = yesterday.atTime(12, 0).toEpochMills
        val end : Long = today.atTime(12, 0).toEpochMills

        //昨天的12H-今日12H
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(fakeOb.copy(
                scheduleStart = start, scheduleEnd = end
            )))


        //創造出expMap
        val list = mutableListOf<TVScheduleModel>(
            //節目先切出一段
            fakeOb.copy(
                scheduleStart = todayStartForNoZoneOffset,
                scheduleEnd = end
            )
        )
        var hStart : Long = end
        while (hStart < todayEndForNoZoneOffset){
            list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }

    /**
     * 只有一節目
     * 此節目是今天的
     */
    @Test
    fun fillGap_max1_today(){
        val start : Long = today.atTime(1, 0).toEpochMills
        val end : Long = today.atTime(2, 0).toEpochMills
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(fakeOb.copy(
                scheduleStart = start, scheduleEnd = end
            )))

        //創造出expMap
        val list = mutableListOf<TVScheduleModel>()
        var hStart : Long = todayStartForNoZoneOffset
        var isAdd = false
        while (hStart < todayEndForNoZoneOffset){
            if ((start until end).contains(hStart)){
                if (!isAdd){
                    list.add(fakeOb.copy(scheduleStart = start, scheduleEnd = end))
                    isAdd = true
                }
            }else{
                list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            }

            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }

    /**
     * 有N節目
     * 都是昨天的節目
     */
    @Test
    fun fillGap_maxN_lastPGN(){
        val start : Long = yesterday.atTime(12, 0).toEpochMills
        val end : Long = yesterday.atTime(13, 0).toEpochMills
        val endest : Long = yesterday.atTime(14, 0).toEpochMills
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(
                fakeOb.copy(scheduleStart = start, scheduleEnd = end),
                fakeOb.copy(scheduleStart = end, scheduleEnd = endest)
            ))

        //創造出expMap
        val list = mutableListOf<TVScheduleModel>()
        var hStart : Long = todayStartForNoZoneOffset
        while (hStart < todayEndForNoZoneOffset){
            list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }

    /**
     * 有N節目
     * 昨天的節目，最後一個是跨日
     */
    @Test
    fun fillGap_maxN_overnight1(){
        val start : Long = yesterday.atTime(12, 0).toEpochMills
        val end : Long = yesterday.atTime(13, 0).toEpochMills
        val start1 : Long = yesterday.atTime(22,0).toEpochMills
        val endest : Long = today.atTime(14, 0).toEpochMills
        //創出跨日
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(
                fakeOb.copy(scheduleStart = start, scheduleEnd = end),
                fakeOb.copy(scheduleStart = start1, scheduleEnd = endest)
            ))

        //創造出expMap
        val list = mutableListOf<TVScheduleModel>(
            //節目先切出一段
            fakeOb.copy(
                scheduleStart = todayStartForNoZoneOffset,
                scheduleEnd = endest
            )
        )
        var hStart : Long = endest
        while (hStart < todayEndForNoZoneOffset){
            list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }

    /**
     * 有Ｎ節目
     * 所有的節目都是今日
     */
    @Test
    fun fillGap_maxN_today(){
        val start : Long = today.atTime(1, 0).toEpochMills
        val end : Long = today.atTime(2, 0).toEpochMills
        val start1 : Long = today.atTime(3, 0).toEpochMills
        val end1 : Long = today.atTime(4, 0).toEpochMills
        val input = hashMapOf<String, List<TVScheduleModel>>(
            chanName to listOf(
                fakeOb.copy(scheduleStart = start, scheduleEnd = end),
                fakeOb.copy(scheduleStart = start1, scheduleEnd = end1)
            ))

        //創造出expMap
        val list = mutableListOf<TVScheduleModel>()
        var hStart : Long = todayStartForNoZoneOffset
        var isAdd = false
        var isAdd1 = false
        while (hStart < todayEndForNoZoneOffset){
            if ((start until end).contains(hStart)){
                if (!isAdd){
                    list.add(fakeOb.copy(scheduleStart = start, scheduleEnd = end))
                    isAdd = true
                }
            }else if ((start1 until end1).contains(hStart)) {
                if (!isAdd1){
                    list.add(fakeOb.copy(scheduleStart = start1, scheduleEnd = end1))
                    isAdd1 = true
                }
            }else {
                list.add(fakeOb.copy(scheduleStart = hStart, scheduleEnd = hStart + 60*60*1000))
            }

            hStart += 60 * 60 * 1000
        }
        val expMap = hashMapOf(chanName to list)


        fieldStart.set(usecase, todayStartForNoZoneOffset)
        fieldEnd.set(usecase, todayEndForNoZoneOffset)
        val output = method.invoke(usecase, input)
        Assert.assertEquals(expMap, output)
    }
}