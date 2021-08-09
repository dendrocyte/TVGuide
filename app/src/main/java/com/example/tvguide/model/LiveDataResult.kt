package com.example.tvguide.model



/**
 * Created by luyiling on 2021/7/20
 * Modified by
 *
 * Description:
 *
 * @params
 * @params
 */

data class LiveDataResult<out T>(val status: Status, val data: T?, val error: Throwable?) {
    companion object {
        fun <T> success(data: T?): LiveDataResult<T> {
            return LiveDataResult(Status.SUCCESS, data, null)
        }

        fun <T> error(e: Throwable?): LiveDataResult<T> {
            return LiveDataResult(Status.ERROR, null, e)
        }
    }
}

enum class Status{
    SUCCESS, ERROR;
}