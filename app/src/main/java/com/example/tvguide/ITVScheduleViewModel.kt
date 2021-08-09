package com.example.tvguide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tvguide.ITVScheduleUsecase
import com.example.tvguide.logd
import com.example.tvguide.model.LiveDataResult
import com.example.tvguide.model.TVListBySchedule
import com.example.tvguide.model.SingleLiveEvent
import usecase.RxTVScheduleUsecase
import io.reactivex.rxjava3.disposables.CompositeDisposable

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
        MutableLiveData<LiveDataResult<List<TVListBySchedule>>>()
    val TVSchedule : LiveData<LiveDataResult<List<TVListBySchedule>>> = mTVSchedule
    val onFinished = SingleLiveEvent<Any>()

    init {
        loadTVSchedule {logd("Fetch Task Complete")}
    }


    internal abstract fun loadTVSchedule(onFinished: () -> Unit)

    fun fetchLiveScheduleByUser() = loadTVSchedule { onFinished.value = Any() }

}