package com.example.tvguide.model

import android.os.Parcelable
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize

/**
 * Created by luyiling on 2021/8/7
 * Modified by
 * PATCH: this model no need -> delete
 * TODO: Live 現在先統一從server 拿取資料塞入，不做太多的ＵＩ間資料傳送
 *
 *
 * Description:
 *
 * @params
 * @params
 */
/*By Schedule*/
data class TVListBySchedule(
    val data : List<TVScheduleModel>,
    val channelName: String
) {

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}


data class TVScheduleModel(
    val channelName: String,
    var scheduleStart : Long,
    var scheduleEnd : Long,
    val program : ProgramModel? = null,
    val playItem : PlayItem? = null,
    val botAnalyst: Analyst? = null
)

data class ProgramModel(
    val title : String,
    val thumbnail : String,
    val desc : String
)

@Parcelize
data class PlayItem(
    var title: String = "",
    var duration : String = "",
    var currentUrl : String = ""
) : Parcelable

@Parcelize
data class Analyst(
    val pid: String //program id
) : Parcelable


/*By Channel*/
/*Program Detail*/
class TVUIModel(
    val channelName: String,
    val meta : TVMetaItem,
    val video : PlayItem,
    val bot: Analyst
){
    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TVUIModel
        if (meta != other.meta) return false
        if (video != other.video) return false
        if (bot != other.bot) return false
        return true
    }

    override fun hashCode(): Int {
        var result = meta.hashCode()
        result = 31 * result + video.hashCode()
        result = 31 * result + bot.hashCode()
        return result
    }
}


data class TVMetaItem(
    var thumbnail : String = "",
    var title : String = "",
    var duration : String = "", //hh:mm:ss
    val description: String = ""
)