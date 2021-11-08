package com.example.tvguide

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.tvguide.model.TVScheduleModel
import io.mockk.*
import org.junit.*
import java.lang.reflect.Field

import java.lang.reflect.Method
import java.time.LocalDate

/**
 * Created by luyiling on 2021/9/24
 * Modified by
 *
 *
 * TODO:
 * Description: createEmptyGap() In LiveScheduleUsecase1
 * NOTE 要測試protected method 裡的邏輯，最好的方式就是使用reflection
 * easily to find out Epoch Milli second is correct or not
 * NOTE check epoch at https://www.epochconverter.com
 *
 * 測試時間點：
 * [start 整-end 非整]
 * 12:00-12:30
 * 12:00-13:30
 * 12:00-15:30
 *
 * [start 整-end 整]
 * 12:00-12:00
 * 12:00-13:00
 * 12:00-14:00
 *
 * [start 非整-end 整]
 * 11:30-13:00
 *
 * [start 非整-end 非整]
 * 11:30-13:30
 * 11:30-14:30
 * 12:20-12:30
 *
 * [一天頭尾時間]
 * 00:00-12:00
 * 21:00-24:00
 * 23:00-24:00
 *
 * [3H 會遇到特殊情境]
 * 12:20-13:00
 * 12:20-14:00
 * 12:20-15:00
 *
 * 邏輯：切出要做出頭中部和尾部
 * @params
 * @params
 */

class CreateEmptyGap1HTest {
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    lateinit var method : Method
    lateinit var field : Field
    lateinit var usecase : ITVScheduleUsecase<*>
    lateinit var chanName : String
    lateinit var fakeOb : TVScheduleModel
    lateinit var today : LocalDate

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
            "create1HEmptyGaps", Long::class.java, Long::class.java, String::class.java
        )
        method.isAccessible = true

        //因為之後都是以 LocalDate 做時間注入 creatEmptyGaps 做邏輯探討
        //LocalDate是 No Zone Offset
        //只要把currentDateStart 更改為timeStartForNoZoneOffset就一致了
        field = ITVScheduleUsecase::class.java.getDeclaredField("currentDateStart")
        field.isAccessible = true

        chanName = "Test Channel"
        fakeOb = TVScheduleModel(
            chanName,
            0,
            0,
            null,
            null,
            null
        )
        today = LocalDate.now()

    }




    //start 整 end 非整, 看times = -1 ; 尾部增添狀況
    @Test
    fun createEmptyGapsTest_1200_1230() {
        val start : Long = today.atTime(12, 0).toEpochMills
        val end = today.atTime(12,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12, 0).toEpochMills,
                scheduleEnd = today.atTime(12, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }




    //start 整 end 非整, 看times = 0 ; 頭尾部增添狀況
    @Test
    fun createEmptyGapsTest_1200_1330() {
        val start : Long = today.atTime(12, 0).toEpochMills
        val end = today.atTime(13,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12, 0).toEpochMills,
                scheduleEnd = today.atTime(13, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(13, 0).toEpochMills,
                scheduleEnd = today.atTime(13, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }




    //start 整 end 非整, 看times > 0 ; 頭中尾部增添是否正確
    @Test
    fun createEmptyGapsTest_1200_1530() {
        val start : Long = today.atTime(12,0).toEpochMills
        val end = today.atTime(15,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12,0).toEpochMills,
                scheduleEnd = today.atTime(13,0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(13, 0).toEpochMills,
                scheduleEnd = today.atTime(14, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(14, 0).toEpochMills,
                scheduleEnd = today.atTime(15, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(15, 0).toEpochMills,
                scheduleEnd = today.atTime(15, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }




    //start 整 end 整, 看times = -1 ; 頭中部增添狀況
    @Test
    fun createEmptyGapsTest_1200_1200() {
        val start : Long = today.atTime(12,0).toEpochMills
        val end = today.atTime(12,0).toEpochMills

        //相同時間點不應有list
        val expList = listOf<TVScheduleModel>()
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }




    //start 整 end 整, 看times = 0 ; 頭中部增添狀況
    @Test
    fun createEmptyGapsTest_1200_1300() {
        val start : Long = today.atTime(12,0).toEpochMills
        val end = today.atTime(13,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12,0).toEpochMills,
                scheduleEnd = today.atTime(13,0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }




    //start 整 end 整, 看times = 1 ; 頭中部增添狀況
    @Test
    fun createEmptyGapsTest_1200_1400() {
        val start : Long = today.atTime(12,0).toEpochMills
        val end = today.atTime(14,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12,0).toEpochMills,
                scheduleEnd = today.atTime(13,0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(13, 0).toEpochMills,
                scheduleEnd = today.atTime(14, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }









    //start 非整 end 整, 看times = 1 ; 頭中部增添狀況
    @Test
    fun createEmptyGapsTest_1130_1300() {
        val start = today.atTime(11, 30).toEpochMills
        val end = today.atTime(13,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(11, 30).toEpochMills,
                scheduleEnd =  today.atTime(12, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(12, 0).toEpochMills,
                scheduleEnd =  today.atTime(13, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }










    //start 非整 end 非整, 看times = -1 ; 尾部增添狀況
    @Test
    fun createEmptyGapsTest_1220_1230() {
        val start = today.atTime(12, 20).toEpochMills
        val end = today.atTime(12,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(12, 20).toEpochMills,
                scheduleEnd =  today.atTime(12, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    //start 非整 end 非整, 看times = 1 ; 頭中尾部增添狀況
    @Test
    fun createEmptyGapsTest_1130_1330() {
        val start = today.atTime(11, 30).toEpochMills
        val end = today.atTime(13,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(11, 30).toEpochMills,
                scheduleEnd =  today.atTime(12, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(12, 0).toEpochMills,
                scheduleEnd =  today.atTime(13, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(13, 0).toEpochMills,
                scheduleEnd =  today.atTime(13, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    //start 非整 end 非整, 看times > 1 ; 頭中尾部增添狀況
    @Test
    fun createEmptyGapsTest_1130_1430() {
        val start = today.atTime(11, 30).toEpochMills
        val end = today.atTime(14,30).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(11, 30).toEpochMills,
                scheduleEnd =  today.atTime(12, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(12, 0).toEpochMills,
                scheduleEnd =  today.atTime(13, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(13, 0).toEpochMills,
                scheduleEnd =  today.atTime(14, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(14, 0).toEpochMills,
                scheduleEnd =  today.atTime(14, 30).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }





    //一天頭尾時間
    @Test
    fun createEmptyGapsTest_0000_1200() {
        val start : Long = today.atTime(0, 0).toEpochMills
        val end = today.atTime(12,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(0, 0).toEpochMills,
                scheduleEnd = today.atTime(1, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(1, 0).toEpochMills,
                scheduleEnd = today.atTime(2, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(2, 0).toEpochMills,
                scheduleEnd = today.atTime(3, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(3, 0).toEpochMills,
                scheduleEnd = today.atTime(4, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(4, 0).toEpochMills,
                scheduleEnd = today.atTime(5, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(5, 0).toEpochMills,
                scheduleEnd = today.atTime(6, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(6, 0).toEpochMills,
                scheduleEnd = today.atTime(7, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(7, 0).toEpochMills,
                scheduleEnd = today.atTime(8, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(8, 0).toEpochMills,
                scheduleEnd = today.atTime(9, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(9, 0).toEpochMills,
                scheduleEnd = today.atTime(10, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(10, 0).toEpochMills,
                scheduleEnd = today.atTime(11, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(11, 0).toEpochMills,
                scheduleEnd = today.atTime(12, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }


    @Test
    fun createEmptyGapsTest_2100_2400() {
        val start = today.atTime(21, 0).toEpochMills
        val end = LocalDate.now().plusDays(1).atTime(0,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(21, 0).toEpochMills,
                scheduleEnd =  today.atTime(22, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(22, 0).toEpochMills,
                scheduleEnd =  today.atTime(23, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart =  today.atTime(23, 0).toEpochMills,
                scheduleEnd =  LocalDate.now().plusDays(1).atTime(0,0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    @Test
    fun createEmptyGapsTest_2300_2400() {
        val start = today.atTime(23, 0).toEpochMills
        val end = LocalDate.now().plusDays(1).atTime(0,0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart =  today.atTime(23, 0).toEpochMills,
                scheduleEnd =  LocalDate.now().plusDays(1).atTime(0,0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    //3H 會遇到特殊情境
    @Test
    fun createEmptyGapsTest_1220_1300() {
        val start = today.atTime(12, 20).toEpochMills
        val end = today.atTime(13, 0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12, 20).toEpochMills,
                scheduleEnd = today.atTime(13, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }


    @Test
    fun createEmptyGapsTest_1220_1400() {
        val start = today.atTime(12, 20).toEpochMills
        val end = today.atTime(14, 0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12, 20).toEpochMills,
                scheduleEnd = today.atTime(13, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(13, 0).toEpochMills,
                scheduleEnd = today.atTime(14, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    @Test
    fun createEmptyGapsTest_1220_1500() {
        val start = today.atTime(12, 20).toEpochMills
        val end = today.atTime(15, 0).toEpochMills

        val expList = listOf(
            fakeOb.copy(
                scheduleStart = today.atTime(12, 20).toEpochMills,
                scheduleEnd = today.atTime(13, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(13, 0).toEpochMills,
                scheduleEnd = today.atTime(14, 0).toEpochMills
            ),
            fakeOb.copy(
                scheduleStart = today.atTime(14, 0).toEpochMills,
                scheduleEnd = today.atTime(15, 0).toEpochMills
            )
        )
        field.set(usecase, todayStartForNoZoneOffset)
        val output = method.invoke(usecase, start, end, chanName)
        Assert.assertEquals(expList, output)
    }



    @After
    fun tearDown() {

    }
}