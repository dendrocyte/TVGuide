package com.example.tvguide.model

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tvguide.logw
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by luyiling on 2021/1/28
 * Modified by
 *
 * Description:
 * Google provide this sample
 * SingleLiveEvent只會發送更新的value，原value若已經發送過就不會再次發送，即避免了configuration change後又顯示一次同樣內容的問題。
 * @bug 因為只會做一次～所以只要更新過一次，即使再次觸發也只會更新原本的observer -> 所以若要adapter裡的某個item更新，就需要將position/原始的ob做更新
 *
 * 問題敘述：
 * 在使用由類中全域型的參數會遇到以舊的參數值做更新
 * 每requestIsDownload一次，只會有一個Event回來
 * 但收到event要處理position，卻仍然是拿取第一次改變的position
 *
 * val ob = vodAdapter.data[position].data
 * viewModel.requestIsDownload(ob?.bot?.id ?:"")
 * println("before query: $position")
 * viewModel.queryDownload.observe(this, Observer{
 * println("query: $position")
 * }
 *
 * 結果：
 * before query: 3
 * query: 3
 * before query: 2
 * query: 3
 * @hide
 * @note https://github.com/android/architecture-samples/blob/todo-mvvm-live/todoapp/app/src/main/java/com/example/android/architecture/blueprints/todoapp/SingleLiveEvent.java
 * @note https://medium.com/@abhiappmobiledeveloper/android-singleliveevent-of-livedata-for-ui-event-35d0c58512da
 * @note https://medium.com/better-programming/how-to-fix-a-serious-problem-in-livedata-android-594a3f18e981
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val mPending = AtomicBoolean(false)
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            logw("Multiple observers registered but only one will be notified of changes.")
        }
        // Observe the internal MutableLiveData
        super.observe(owner) { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }
    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }
    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        setValue(null)
    }
}
