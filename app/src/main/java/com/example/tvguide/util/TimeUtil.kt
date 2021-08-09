package com.example.tvguide

import java.lang.Exception
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)+1
        val day = today.get(Calendar.DATE)
        today.set(year,month,day,0 ,0)
        println("today start: ${today.timeInMillis}")
        return today.timeInMillis
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
            else -> throw Exception("Noovo: Illegal time format")
        }