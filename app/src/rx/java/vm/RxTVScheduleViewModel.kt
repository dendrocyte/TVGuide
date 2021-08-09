package vm

import com.example.tvguide.model.LiveDataResult
import com.example.tvguide.ITVScheduleViewModel
import usecase.RxTVScheduleUsecase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Created by luyiling on 2020/9/21
 * Modified by
 *
 * PATCH: 看koin 注入分配
 * Description:
 *
 *
 * @params
 */
class RxTVScheduleViewModel(
    usecase: RxTVScheduleUsecase
) : ITVScheduleViewModel(usecase) {

    lateinit var disposable : CompositeDisposable

    init {
        //如果父有做init{} 裡呼叫子的實踐方法，就會跳過上面宣告disposable
    }

    override fun loadTVSchedule(onFinished: () -> Unit){
        disposable = CompositeDisposable()
        disposable.add(
            (usecase as RxTVScheduleUsecase).getSchedule()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate { onFinished() }
                .subscribe({
                    mTVSchedule.postValue(LiveDataResult.success(it))
                },{
                    mTVSchedule.postValue(LiveDataResult.error(it))
                })
        )
    }
    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}