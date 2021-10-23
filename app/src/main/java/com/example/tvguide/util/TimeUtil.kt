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
            else -> throw Exception("toEpochMills: Illegal time format")
        }

val Any.toHHmm : String
    get() = when(this){
        //Mili-Second
        is Long -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        else -> throw Exception("toHHmm: Illegal time format")
    }