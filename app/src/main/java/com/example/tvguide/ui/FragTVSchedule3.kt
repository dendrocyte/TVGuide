package com.example.tvguide.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvguide.ITVScheduleViewModel
import com.example.tvguide.R
import com.example.tvguide.adapter.ChannelAdapter
import com.example.tvguide.adapter.ProgramTableAdapter
import com.example.tvguide.adapter.TimelineAdapter
import com.example.tvguide.custom.Schedule
import com.example.tvguide.custom.Timeline
import com.example.tvguide.databinding.FragLiveScheduleMyDesign1Binding
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
 * NOTE **Design 3**
 * 用RV取代上下滑動
 * Description: With My Design UI/UX
 * @params
 * @params
 */
class FragTVSchedule3 : Fragment(){
    //若xml名稱有底線，會去除以Java的格式命名
    private var _binding: FragLiveScheduleMyDesign1Binding? = null
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
    private val programTableAdapter : ProgramTableAdapter by lazy {
        ProgramTableAdapter(hashMapOf()).apply {
            setOnItemChildClickListener { adapter, v, position ->
                //TODO: navi channel
            }
            setDiffCallback(object : DiffUtil.ItemCallback<Map.Entry<String, List<TVScheduleModel>>>(){
                override fun areItemsTheSame(
                    oldItem: Map.Entry<String, List<TVScheduleModel>>,
                    newItem: Map.Entry<String, List<TVScheduleModel>>
                ): Boolean {
                    return oldItem == newItem
                }
                override fun areContentsTheSame(
                    oldItem: Map.Entry<String, List<TVScheduleModel>>,
                    newItem: Map.Entry<String, List<TVScheduleModel>>
                ): Boolean {
                    return oldItem.hashCode() == newItem.hashCode()
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragLiveScheduleMyDesign1Binding.inflate(inflater, container, false)
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

        //Feature: Prgms (RV) scroll, chans (RV) scroll
        with(binding.recyclerProgramTable) {
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

        //Feature: Chan (RV) scroll, Prgm (RV) scroll
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


        //校正對齊線
        binding.recyclerProgramTable.correctStartLine(false)

        recycler@ with(binding.recyclerProgramTable) {
            adapter = programTableAdapter

            adapter@ with(programTableAdapter){
                this.passiveHScrollSubject = this@FragTVSchedule3.passiveHScrollSubject
                setDiffNewData(data.entries.toMutableList())

            }
        }
    }


    private fun feedEmptyErrData(isErr: Boolean){
        //這樣Animation 才不會不見
        val isSameErrorShowing =
                programTableAdapter.emptyLayout?.findViewById<TextView>(R.id.tVDescript)?.run {
                    if (isErr)
                        text == resources.getText(R.string.lost_connection)
                    else
                        text == resources.getText(R.string.no_epg)
                } ?: false
        if (isSameErrorShowing) return

        //校正對齊線
        binding.recyclerProgramTable.correctStartLine(true)

        val layout = if (isErr) R.layout.frag_empty_error1 else R.layout.frag_empty_error3
        val errView : View = layoutInflater.inflate(layout, binding.recyclerProgramTable, false)
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

        recycler@ with(binding.recyclerProgramTable) {
            adapter = programTableAdapter

            adapter@ with(programTableAdapter){
                this.passiveHScrollSubject = this@FragTVSchedule3.passiveHScrollSubject
                this.setEmptyView(errView)
                setDiffNewData(mutableListOf())
            }
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