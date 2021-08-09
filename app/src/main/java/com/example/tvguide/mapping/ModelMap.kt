package com.example.tvguide.mapping

import com.example.tvguide.model.*
import com.example.tvguide.remote.Program
import com.example.tvguide.toEpochMills

/**
 * Created by luyiling on 2021/8/6
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
object ModelMap {

    fun toTVScheduleModel(program: Program): TVScheduleModel {
        return TVScheduleModel(
            program.channel,
            program.start.toEpochMills,
            program.stop.toEpochMills,
            ProgramModel(
                program.title,
                program.cover,
                program.desc
            ),
            PlayItem(
                program.title,
                program.length,
                program.url
            ),
            Analyst(program.pid)
        )
    }

    fun toTVUIModel(program: Program): TVUIModel{
        return TVUIModel(
            program.channel,
            TVMetaItem(
                program.cover,
                program.title,
                program.length,
                program.desc
            ),
            PlayItem(
                program.title,
                program.length,
                program.url
            ),
            Analyst(program.pid)
        )
    }

}

