package com.example.tvguide.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginStart
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.R
import com.example.tvguide.adapter.ProgramAdapter
import com.example.tvguide.adapter.TimelineAdapter
import com.example.tvguide.custom.LineMarkerAgent
import com.example.tvguide.custom.Schedule
import com.example.tvguide.custom.Timeline
import com.example.tvguide.databinding.FragLiveScheduleBinding
import com.example.tvguide.logd
import com.example.tvguide.model.Status
import com.example.tvguide.ITVScheduleViewModel
import com.example.tvguide.custom.ScaleAgent
import com.example.tvguide.model.TVScheduleModel
import com.example.tvguide.vm.LiveShareViewModel
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.sharedStateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.sql.DriverManager.println


/**
 * Created by luyiling on 2020/9/18
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @must 必定要用nestScrollView 動態填入view 模擬recyclerview 的功能
 * 使得recyclerview 單一層，故可以監控scroll
 * @note 試過後覺得這個ＵＩ架構比較平坦: scrollview dynamically add (R.layout.single_live_schedule1)
 */
class FragTVSchedule1 : Fragment(){
    //若xml名稱有底線，會去除以Java的格式命名
    private var _binding: FragLiveScheduleBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val shareViewModel : LiveShareViewModel by sharedStateViewModel()
    private val viewModel : ITVScheduleViewModel by viewModel()
    private val timelineAdapter by lazy { TimelineAdapter(viewModel.timeline) }

    /**
     * 發起者移動多少 x量
     * Pair<Int, Int> = invoker to dx
     * timeline: pass idRes to dx
     * schedule: pass index to dx
     */
    private val passiveScrollSubject = PublishSubject.create<Pair<Int, Int>>()
    private var disposable : Disposable? = null


    private lateinit var agent: LineMarkerAgent
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragLiveScheduleBinding.inflate(inflater, container, false)
        return binding.getRoot()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        feedLoadingView()

        viewModel.TVSchedule.observe(viewLifecycleOwner){
            logd("recv: $it")
            when(it.status){
                Status.SUCCESS->
                    if (it.data?.isNotEmpty() == true) feedData(it.data)
                    else feedEmptyErrData(isErr = false)

                Status.ERROR-> feedEmptyErrData(isErr = true)
            }

        }


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


        /*line marker*/
        with(binding.lineMarker){

            measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED)
            )
            agent = LineMarkerAgent(marginStart.toFloat()).also {
                //極限值
                it.xStart = binding.recyclerTimeline.marginStart.toFloat()
                it.xEnd = resources.displayMetrics.widthPixels.toFloat()-measuredWidth
                setOnTouchListener(it.touchListener)
                //Feature:顯示目前line marker時間
                binding.tVIndicator.text =
                    calIndicatorTiming(binding.recyclerTimeline.overallScroll, it.newdX)
                //Feature: line marker 移動，indicator 指標時間跟著移動
                it.newXListener = { d ->
                    binding.tVIndicator.text =
                        calIndicatorTiming(binding.recyclerTimeline.overallScroll, d)
                }
            }

        }

    }

    private fun modifyScrollView(startId: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)
        constraintSet.connect(R.id.scrollView, ConstraintSet.START, startId, ConstraintSet.START)
        constraintSet.connect(
            R.id.scrollView,
            ConstraintSet.START,
            R.id.recyclerTimeline,
            ConstraintSet.START
        )
        constraintSet.applyTo(binding.root)
    }


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
            //1H = 螢幕 * 0.66
            if (newState == RecyclerView.SCROLL_STATE_IDLE){
                with(rv as Timeline){
                    /*主動scroll 要通知schedule recyclerview*/
                    kotlin.io.println("timeline Scroll stop")
                    binding.tVIndicator.text = calIndicatorTiming(overallScroll, agent.newdX)
                    passiveScrollSubject.onNext(binding.recyclerTimeline.id to shiftX)
                    shiftX = 0 //歸零
                }
            }
        }
        //Feature: schedule 移動,.., indicator的時間改變
        override fun onPassiveScrollState(rv: RecyclerView, state: Int) {
            with(rv as Timeline){
                kotlin.io.println("timeline Scroll stop")
                shiftX = 0 //歸零
                binding.tVIndicator.text = calIndicatorTiming(overallScroll, agent.newdX)
            }
        }
    }





    /**
     *
     * @param overallScroll  = timeline移動距離
     * from recyclerview (px)
     * @param dx  = 目前螢幕上timeline的起點與line marker的距離
     * from line marker (px)
     *
     * default = timelineAdapter[0]的時間點+ (line marker的x / 1H的dp段)*60min
     * movement = timelineAdapter[0]的時間點+ overall scroll+ (line marker的x / 1H的dp段)*60min
     */
    fun calIndicatorTiming(overallScroll: Float, dx : Float) : String{
        logd("overall: $overallScroll, $dx")
        //量定稿比例
        val pxToH = 1f/ScaleAgent.pxOf1HWidth

        val shift = (overallScroll+dx)*pxToH
        //小數點前
        val i = shift.toInt()
        //小數點後
        val j = shift - i
        logd("\nshift hour = $i\nshift min = ${j*60}")

        val extract = timelineAdapter.data[0].time.split(":")
        val hh = String.format("%02d", extract[0].toInt()+i)
        val mm = String.format("%02d", extract[1].toInt()+(j*60).toInt())
        val result = "$hh:$mm"
        logd("cal time = $result")
        return result
    }



    /*+++++++++++++++++++ feed data ++++++++++++++++++++*/
    fun feedData(data : Map<String, List<TVScheduleModel>>) {
        //modify scrollView margin
        modifyScrollView(R.id.recyclerTimeline)
        //若xml裡view名稱有底線，會去除以Java的格式命名
        binding.containerSchedule.removeAllViews()
        data.forEach { info ->
            //add live_schedule
            val view =
                layoutInflater.inflate(R.layout.single_live_schedule, binding.containerSchedule, false)

            with(view.findViewById<TextView>(R.id.tVChannel)) {
                text = info.key
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

                //因single_tick_start 前距 margin start = 7dp + 13dp 第一點的半徑 = 20dp
                addItemDecoration(MarginDecoation((20 * resources.displayMetrics.density).toInt())) //校正對期線

                //因為每一個的schedule recyclerview的id都一樣，要用tag 做區分
                tag = info.key.hashCode()

                //因為embedded recyclerview
                // (如: 父recyclerview + 子recyclerview) (如：父 nestScrollView + 子recyclerview)
                // 子recyclerview 都會沒收到ScrollListener, 需做客製化
                offsetListener = object : Schedule.OffsetScrollListener{
                    override fun onScrolled(dx: Int, dy: Int, accOffsetX: Int, accOffsetY: Int) {
                        /*主動被動都會invoke*/
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
                binding.containerSchedule.addView(view)

            }
        }

    }


    private fun feedEmptyErrData(isErr: Boolean){
        //modify scrollView margin
        modifyScrollView(R.id.root)

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

    private fun feedLoadingView(){
        binding.containerSchedule.removeAllViews()
        repeat(3){
            val view = layoutInflater.inflate(R.layout.shimmer_schedule, binding.containerSchedule, false)
            val param = view.layoutParams as ViewGroup.LayoutParams
            param.width = resources.displayMetrics.widthPixels
            binding.containerSchedule.addView(view)
        }
    }

    inner class MarginDecoation(@androidx.annotation.IntRange(from = 0) var margin: Int)
        : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
            if(itemPosition == 0) outRect.left = margin
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerTimeline.clearOnScrollListeners()
        agent.newXListener = null
        disposable?.dispose()
        //Fragment 的存在时间比其视图长。请务必在 Fragment 的 onDestroyView() 方法中清除对绑定类实例的所有引用。
        _binding = null
    }
}