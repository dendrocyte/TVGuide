package com.example.tvguide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tvguide.model.LiveDataResult
import com.example.tvguide.model.SingleLiveEvent
import com.example.tvguide.model.TVScheduleModel

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * PATCH: Make a interface, move this to rx
 * Description: 由rx/, coroutine/ 實作; main/ 呼叫 ITVScheduleViewModel 即可
 *
 * @params
 */
abstract class ITVScheduleViewModel(protected val usecase: ITVScheduleUsecase<*>) : ViewModel() {

    val timeline = usecase.timeline


    protected val mTVSchedule =
        MutableLiveData<LiveDataResult<Map<String, List<TVScheduleModel>>>>()
    val TVSchedule : LiveData<LiveDataResult<Map<String, List<TVScheduleModel>>>> = mTVSchedule
    val onFinished = SingleLiveEvent<Any>()

    init {
        loadTVSchedule {logd("Fetch Task Complete")}
    }


    internal abstract fun loadTVSchedule(onFinished: () -> Unit)

    fun fetchLiveScheduleByUser() = loadTVSchedule { onFinished.value = Any() }

}