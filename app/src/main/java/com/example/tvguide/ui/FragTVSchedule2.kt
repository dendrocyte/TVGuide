package com.example.tvguide.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.ITVScheduleViewModel
import com.example.tvguide.R
import com.example.tvguide.adapter.ChannelAdapter
import com.example.tvguide.adapter.ProgramAdapter
import com.example.tvguide.adapter.TimelineAdapter
import com.example.tvguide.custom.Schedule
import com.example.tvguide.custom.Timeline
import com.example.tvguide.databinding.FragLiveScheduleMyDesignBinding
import com.example.tvguide.logd
import com.example.tvguide.model.Status
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.vm.LiveShareViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.sharedStateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by luyiling on 2021/10/9
 * Modified by
 * NOTE **Design 2**
 * 用NestSV取代上下滑動
 * FIXME: 但NestSV 沒有
 * TODO: 測試NextScrollView & HorizontalScrollView 對 Scan Vertical & Horizontal 的偵測 (which listener)
 * Description: With My Design UI/UX
 * @params
 * @params
 */
class FragTVSchedule2 : Fragment(){
    //若xml名稱有底線，會去除以Java的格式命名
    private var _binding: FragLiveScheduleMyDesignBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * @orientation: horizontal
     * 發起者移動多少 x量
     * Pair<Int, Int> = invoker to dx
     * timeline: pass idRes to dx
     * schedule: pass index to dx
     */
    private val passiveHScrollSubject = PublishSubject.create<Pair<Int, Int>>()
    /**
     * @orientation: vertical
     * 發起者移動多少 y量
     * Pair<Int, Int> = invoker to dy
     * channel: pass idRes to dy
     * schedule: pass index to dy
     */
    private val passiveVScrollSubject = PublishSubject.create<Pair<Int, Int>>()
    private var disposable : Disposable? = null

    private val shareViewModel : LiveShareViewModel by sharedStateViewModel()
    private val viewModel : ITVScheduleViewModel by viewModel()
    private val timelineAdapter by lazy { TimelineAdapter(viewModel.timeline) }
    private val channelAdapter by lazy {
        ChannelAdapter(setOf()).apply {
            setOnItemChildClickListener { adapter, v, position ->
                //TODO: navi channel
            }
            setDiffCallback(object : DiffUtil.ItemCallback<String>(){
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                   return oldItem == newItem
                }
                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem.hashCode() == newItem.hashCode()
                }
            })
        }}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragLiveScheduleMyDesignBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerTimeline){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = timelineAdapter
            addStateScrollListener(timeLineScrollListener)
            disposable = passiveHScrollSubject.subscribe {
                logd("timeline: recv passive scroll...")
                if (it.first != id) scrollBy(it.second,0)
            }
            /*即使是schedule recyclerview 捲動也會在接收到*/
//            viewTreeObserver.addOnScrollChangedListener { }
        }

        //NOTE NestSV's ScrollChangeListener 可以讓RV 直接跟手, 會有NestSV 和 RV 同步移動的效果
        //Feature: Prgms (NestSV) scroll, chans (RV) scroll
        with(binding.nestScrollView) {
            setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                //NOTE 主動被動都會invoke, 用flag 來區別
                //scroll vertical, sync with program vertical
                logd("Scroll Type: ${this.scrollType}")
                if (this.scrollType == ViewCompat.TYPE_TOUCH) {
                    passiveVScrollSubject.onNext(this.id to scrollY - oldScrollY)
                }
            })
            passiveVScrollSubject.subscribe {
                logd("channel: recv passive scroll...")
                //不可以用smoothScrollBy 移動的數值會不對
                if (it.first != this.id) scrollBy(0,it.second)
            }
        }

        //NOTE RV 主動滑時會與NestSV 不同步滑動起點，但會划動相同距離
        with(binding.recyclerChannel){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            offsetListener = object : Schedule.OffsetScrollListener{
                override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                    //主動被動都會invoke
                }
                override fun onScrollIDEState(rv: RecyclerView, shiftX: Int, shiftY: Int) {
                    passiveVScrollSubject.onNext(this@with.id to shiftY)
                }
            }
            passiveVScrollSubject.subscribe {
                logd("channel: recv passive scroll...")
                //不可以用smoothScrollBy 移動的數值會不對
                if (it.first != this@with.id) scrollBy(0,it.second)
            }
        }

        viewModel.TVSchedule.observe(viewLifecycleOwner){
            logd("recv: $it")
            when(it.status){
                Status.SUCCESS->
                    if (it.data?.isNotEmpty() == true) feedData(it.data)
                    else feedEmptyErrData(isErr = false)

                Status.ERROR-> feedEmptyErrData(isErr = true)
            }

        }


    }

    /////////////////////////////// Timeline ////////////////////////////////////////

    private val timeLineScrollListener = object : Timeline.ScrollListener{
        //schedule 移動, timeline移動 ->推算移動總量
        //User捲動timeline -> 推算移動總量
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            with(rv as Timeline){
                overallScroll += dx
                shiftX += dx
                println("timeline: scroll $overallScroll")
            }
        }
        //Feature: timeline 移動，indicator的時間也會改變
        override fun onActiveScrollStateChanged(rv: RecyclerView, newState: Int) {
            //1H = 螢幕 * 0.29
            if (newState == RecyclerView.SCROLL_STATE_IDLE){
                with(rv as Timeline){
                    /*主動scroll 要通知schedule recyclerview*/
                    println("timeline Scroll stop")
                    passiveHScrollSubject.onNext(binding.recyclerTimeline.id to shiftX)
                    shiftX = 0 //歸零
                }
            }
        }
        //Feature: schedule 移動,.., indicator的時間改變
        override fun onPassiveScrollState(rv: RecyclerView, state: Int) {
            with(rv as Timeline){
                println("timeline Scroll stop")
                shiftX = 0 //歸零
            }
        }
    }

    /////////////////////////// feed data ///////////////////////////////////////////


    fun feedData(data : Map<String, List<TVScheduleModel>>) {
        //TEST pass: 資料是一對的
        with(binding.recyclerChannel) {
            adapter = channelAdapter
            channelAdapter.setDiffNewData(data.keys.toMutableList())
        }



        //若xml裡view名稱有底線，會去除以Java的格式命名
        binding.containerSchedule.removeAllViews()

        //校正對齊線
        binding.nestScrollView.correctStartLine(false)

        data.entries.forEach { info ->
            //add live_schedule
            val view =
                layoutInflater.inflate(R.layout.single_live_schedule_my_design, binding.containerSchedule, false)

            with(view.findViewById<Schedule>(R.id.recyclerProgram)) {
                logd("child schedule id: ${this.id}")
                setHasFixedSize(false)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = ProgramAdapter(info.value).apply {
                    setOnItemChildClickListener { adapter, view, position ->
                        logd("$view:Thumbnail click---$position")
                        //FIXME
//                        startActivity(intentFor<PlayerActivity>(
//                            ARG_VIDEO to this.data[position].playItem,
//                            ARG_ANAL to this.data[position].botAnalyst
//                        ))
                    }
                }




                //因為每一個的schedule recyclerview的id都一樣，要用tag 做區分
                tag = info.key.hashCode()

                //FIXME: 移動到父類去處理(目前找不到)
                //因為embedded recyclerview
                // (如: 父recyclerview + 子recyclerview) (如：父 nestScrollView + 子recyclerview)
                // 子recyclerview 都會沒收到ScrollListener, 需做客製化
                offsetListener = object : Schedule.OffsetScrollListener{
                    override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                        //主動被動都會invoke
                    }
                    override fun onScrollIDEState(rv: RecyclerView, shiftX: Int, shiftY: Int) {
                        passiveHScrollSubject.onNext(tag as Int to shiftX)
                    }
                }
                passiveHScrollSubject.subscribe {
                    logd("schedule: recv passive scroll...")
                    //不可以用smoothScrollBy 移動的數值會不對
                    if (it.first != this.tag as Int) scrollBy(it.second,0)
                }
                binding.containerSchedule.addView(view)

            }
        }

    }


    private fun feedEmptyErrData(isErr: Boolean){
        //這樣Animation 才不會不見
        val isSameErrorShowing =
                binding.containerSchedule.findViewById<TextView>(R.id.tVDescript)?.run {
                    if (isErr)
                        text == resources.getText(R.string.lost_connection)
                    else
                        text == resources.getText(R.string.no_epg)
                } ?: false
        println("isSameErr : $isSameErrorShowing")
        if (isSameErrorShowing) return

        binding.nestScrollView.correctStartLine(true)

        binding.containerSchedule.removeAllViews()
        val layout = if (isErr) R.layout.frag_empty_error1 else R.layout.frag_empty_error3
        val errView = layoutInflater.inflate(layout, binding.containerSchedule, false)
        val param = errView.layoutParams as ViewGroup.LayoutParams
        param.width = resources.displayMetrics.widthPixels
        val loading = errView.findViewById<ProgressBar>(R.id.loading)
        errView.findViewById<ImageButton>(R.id.iBRetry).setOnClickListener { view ->
            //fetch vod list: shall be Unit not ()-> Unit
            logd("Fetch...")
            //make the retry btn to wait fetch result
            view.animate().alpha(0f).setDuration(100).start()
            loading.animate().alpha(1f).setDuration(100).start()

            viewModel.fetchLiveScheduleByUser()
            viewModel.onFinished.observe(viewLifecycleOwner) {
                logd("Fetch list done by User")
                view.postDelayed({
                    if (isResumed) {
                        view.animate().alpha(1f).setDuration(500).start()
                        loading.animate().alpha(0f).setDuration(1000).start()
                    }
                }, 300)
            }
        }

        binding.containerSchedule.addView(errView)
    }

    //TESTME: loading UI
    private fun feedLoadingView(){
        binding.containerSchedule.removeAllViews()
        repeat(3){
            val view = layoutInflater.inflate(R.layout.shimmer_schedule, binding.containerSchedule, false)
            val param = view.layoutParams as ViewGroup.LayoutParams
            param.width = resources.displayMetrics.widthPixels
            binding.containerSchedule.addView(view)
        }
    }




    private fun View.correctStartLine(isErrorPage : Boolean){
        if(isErrorPage){
            //右移對齊parent

            val set = ConstraintSet()
            set.clone(binding.root)
            set.connect(this.id, ConstraintSet.START,R.id.root, ConstraintSet.START)
            set.applyTo(binding.root)
        }else {
            //右移對齊tick icon的中心

            //偵測tick icon 得到要調整的間距 (icon 隨不同機體會用相對應的大小)
            val tick = layoutInflater.inflate(R.layout.single_tick_start, null)
            tick.findViewById<TextView>(R.id.tVtick).text = "00:00"
            val pin = tick.findViewById<ImageView>(R.id.iVpin)
            tick.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            println("TEST: ${(pin.layoutParams as ViewGroup.MarginLayoutParams).leftMargin} + ${(pin.measuredWidth /2f)}")
            val margin = (pin.layoutParams as ViewGroup.MarginLayoutParams).leftMargin + (pin.measuredWidth /2f)

            val set = ConstraintSet()
            set.clone(binding.root)
            set.connect(this.id, ConstraintSet.START,R.id.recyclerChannel, ConstraintSet.START, margin.toInt())
            set.applyTo(binding.root)
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerTimeline.clearOnScrollListeners()
        disposable?.dispose()
        //Fragment 的存在时间比其视图长。请务必在 Fragment 的 onDestroyView() 方法中清除对绑定类实例的所有引用。
        _binding = null
    }
}