package com.example.tvguide

import java.lang.Exception
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Created by luyiling on 2021/2/4
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */

val todayStart : Long
    get() {
        //truncatedTo 會將設定的單位（Day）後都清為0
        //如：truncatedTo(Day) 將Day, Hour, Minute, Second, NanoSecond 都清為0
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        println("${today.year}/${today.month}/${today.dayOfMonth}")
        println("today start: ${today.toEpochMills}")
        return today.toEpochMills
    }

/**
 * 測試時以GMT+0來測試不影響測試的邏輯
 */
val todayStartForNoZoneOffset : Long
    get() {
        //取得GMT 起始點且為 00:00 的時間
        val today = LocalDate.now()
        //.now()已是該GMT時區 00:00, 但若要到目前時區的 GMT+8
        today.logd("${today.year}/${today.month}/${today.dayOfMonth}")
        today.logd("today start: ${today.toEpochMills}")
        return today.toEpochMills
    }
/**
 * 測試時以GMT+0來測試不影響測試的邏輯
 */
val todayEndForNoZoneOffset : Long
    get() {
        return todayStartForNoZoneOffset+(24*60*60*1000)
    }

val today24HEnd : Long
    get() {
        //避免遇到一個月的最後一天
        return todayStart+(24*60*60*1000)
    }

val today25HEnd : Long
    get() {
        //避免遇到一個月的最後一天
        return todayStart+(25*60*60*1000)
    }

val today1HEnd : Long
    get() {
        //避免遇到一個月的最後一天
        return todayStart+(1*60*60*1000)
    }

val Any.toEpochMills : Long
    get() = when(this){
            //shift timezone and change time format
            is String -> LocalDateTime
                .parse(this)
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toEpochSecond() * 1000 //ms
            //SimpleDateFormat.getInstance().parse(this).time
            is Long -> this
            is Date -> this.time
            is ZonedDateTime -> this.toEpochSecond() * 1000
            is LocalDate -> this.toEpochDay() * 24 * 60 * 60 * 1000
            is LocalDateTime -> this.toEpochSecond(ZoneOffset.UTC) * 1000
            else -> throw Exception("toEpochMills: Illegal time format")
        }

val Any.toHHmm : String
    get() = when(this){
        //Mili-Second
        is Long -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        else -> throw Exception("toHHmm: Illegal time format")
    }