package com.example.tvguide.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.ITVScheduleViewModel
import com.example.tvguide.R
import com.example.tvguide.adapter.ProgramAdapter
import com.example.tvguide.adapter.TimelineAdapter
import com.example.tvguide.custom.Schedule
import com.example.tvguide.custom.Timeline
import com.example.tvguide.databinding.FragLiveScheduleMyDesignBinding
import com.example.tvguide.logd
import com.example.tvguide.model.Status
import com.example.tvguide.model.TVListBySchedule
import com.example.tvguide.vm.LiveShareViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.sharedStateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by luyiling on 2021/10/9
 * Modified by
 *
 * TODO:
 * Description: With My Design UI/UX
 *
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
     * 發起者移動多少 x量
     * Pair<Int, Int> = invoker to dx
     * timeline: pass idRes to dx
     * schedule: pass index to dx
     */
    private val passiveScrollSubject = PublishSubject.create<Pair<Int, Int>>()
    private var disposable : Disposable? = null

    private val shareViewModel : LiveShareViewModel by sharedStateViewModel()
    private val viewModel : ITVScheduleViewModel by viewModel()
    private val timelineAdapter by lazy { TimelineAdapter(viewModel.timeline) }


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

        feedLoadingView()

        with(binding.recyclerTimeline){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = timelineAdapter
            addStateScrollListener(timeLineScrollListener)
            disposable = passiveScrollSubject.subscribe {
                logd("timeline: recv passive scroll...")
                if (it.first != id) scrollBy(it.second,0)
            }
            /*即使是schedule recyclerview 捲動也會在接收到*/
//            viewTreeObserver.addOnScrollChangedListener { }
        }


        viewModel.TVSchedule.observe(viewLifecycleOwner){
            logd("recv: $it")
            when(it.status){
                Status.SUCCESS->
                    if (it.data?.isNotEmpty() == true) feedData(it.data.toMutableList())
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
                    passiveScrollSubject.onNext(binding.recyclerTimeline.id to shiftX)
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


    fun feedData(data : MutableList<TVListBySchedule>) {
        //FIXME modify scrollView margin?

        //若xml裡view名稱有底線，會去除以Java的格式命名
        binding.containerSchedule.removeAllViews()
        data.forEachIndexed { index, info ->
            //FIXME: add RV??
            //add live_schedule
            val view =
                layoutInflater.inflate(R.layout.single_live_schedule_my_design, binding.containerSchedule, false)

            with(view.findViewById<TextView>(R.id.tVChannel)) {
                text = info.channelName
                setOnClickListener {
                    logd("Channel click: ${this.text}")
                    shareViewModel.naviChannel.onNext(this.text.toString())
                }
            }

            with(view.findViewById<Schedule>(R.id.recyclerProgram)) {
                logd("child schedule id: ${this.id}")
                val manager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                layoutManager = manager
                adapter = ProgramAdapter(info.data).apply {
                    setOnItemChildClickListener { adapter, view, position ->
                        logd("$view:Thumbnail click---$position")
                        //FIXME
//                        startActivity(intentFor<PlayerActivity>(
//                            ARG_VIDEO to this.data[position].playItem,
//                            ARG_ANAL to this.data[position].botAnalyst
//                        ))
                    }
                }

                //FIXME: 校正對期限
                //因single_tick_start 前距 margin start = 7dp + 13dp 第一點的半徑 = 20dp
                //FIXME: addItemDecoration(MarginDecoation((20 * resources.displayMetrics.density).toInt())) //校正對期線

                //因為每一個的schedule recyclerview的id都一樣，要用tag 做區分
                tag = index

                //FIXME: 移動到父類去處理
                //因為embedded recyclerview
                // (如: 父recyclerview + 子recyclerview) (如：父 nestScrollView + 子recyclerview)
                // 子recyclerview 都會沒收到ScrollListener, 需做客製化
                /*offsetListener = object : Schedule.OffsetScrollListener{
                    override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                        *//*主動被動都會invoke*//*
                    }
                    override fun onScrollIDEState(rv: RecyclerView, shiftX: Int) {
                        passiveScrollSubject.onNext(tag as Int to shiftX)
                    }
                }
                passiveScrollSubject.subscribe {
                    logd("schedule: recv passive scroll...")
                    //不可以用smoothScrollBy 移動的數值會不對
                    if (it.first != this.tag as Int) scrollBy(it.second,0)
                }
                */
                binding.containerSchedule.addView(view)

            }
        }

    }


    private fun feedEmptyErrData(isErr: Boolean){
        //FIXME modify scrollView margin?
        //modify scrollView margin
        //modifyScrollView(R.id.root)

        binding.containerSchedule.removeAllViews()
        val layout = if (isErr) R.layout.frag_empty_error1 else R.layout.frag_empty_error3
        val errView = layoutInflater.inflate(layout, binding.containerSchedule, false)
        val param = errView.layoutParams as ViewGroup.LayoutParams
        param.width = resources.displayMetrics.widthPixels
        if (!isErr) errView.findViewById<TextView>(R.id.tVDescript).text = "no schedule available"
        val loading = errView.findViewById<ProgressBar>(R.id.loading)
        errView.findViewById<Button>(R.id.btnRetry).setOnClickListener { view ->
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

    //FIXME: loading UI
    private fun feedLoadingView(){
        binding.containerSchedule.removeAllViews()
        repeat(3){
            val view = layoutInflater.inflate(R.layout.shimmer_schedule, binding.containerSchedule, false)
            val param = view.layoutParams as ViewGroup.LayoutParams
            param.width = resources.displayMetrics.widthPixels
            binding.containerSchedule.addView(view)
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