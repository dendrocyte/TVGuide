package com.example.tvguide.remote
import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime


/**
 * Created by luyiling on 2021/8/5
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
data class ResponseModel(
    @SerializedName("channel")
    val channel: String = "",
    @SerializedName("programs")
    val programs: List<Program> = emptyList()
)

data class Program(
    @SerializedName("channel")
    val channel: String = "",
    @SerializedName("cover")
    val cover: String = "",
    @SerializedName("date")
    val date: String ="",
    @SerializedName("desc")
    val desc: String ="",
    @SerializedName("lang")
    val lang: String ="",
    @SerializedName("length")
    val length: String ="",
    @SerializedName("pid")
    val pid: String ="",
    @SerializedName("start")
    val start: ZonedDateTime,
    @SerializedName("stop")
    val stop: ZonedDateTime,
    @SerializedName("title")
    val title: String ="",
    @SerializedName("url")
    val url: String =""
)