package com.example.tvguide.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tvguide.ITVScheduleUsecase
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Created by luyiling on 2020/9/18
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class LiveShareViewModel(
    private val scheduleUsecase: ITVScheduleUsecase<*>
) : ViewModel() {


    /**
     * navi to By Channel
     * value: channelName
     */
    var naviChannel = PublishSubject.create<String>()


    /**
     * FIXME
     * filter chip : not well-defined
     */
    var filterBy = MutableLiveData("All")

    val filterMenuList = scheduleUsecase.filterMenuList


}