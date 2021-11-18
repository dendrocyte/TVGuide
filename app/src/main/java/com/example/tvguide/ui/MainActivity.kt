package com.example.tvguide.ui


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.tvguide.R
import com.example.tvguide.databinding.ActivityMainBinding
import com.example.tvguide.logd
import com.example.tvguide.model.MenuModel
import com.example.tvguide.navi
import com.example.tvguide.vm.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModel()
    private var drawerListenr : DrawerLayout.DrawerListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //要在activity賦予實體，並把binding.root塞入setContentView() 才行，否則會找不到id
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //若不用view binding, 要在layout 加註tools:viewBindingIgnore
        //如原本列入contentView(@layoutRes)
        //setContentView(R.layout.activity_main)



        /////////////////////// 初始化drawer /////////////////////////////

        with(binding.drawer){

            for(item in viewModel.menuList){
                if (item.isSelected) itemOn(item.title)
            }
        }



        with(binding.drawer.findViewById<RecyclerView>(R.id.recyclerDrawer)){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            logd("drawer menu size: ${viewModel.menuList.size}")
            adapter = MenuAdapter(viewModel.menuList).apply {
                setDiffCallback(object : DiffUtil.ItemCallback<MenuModel>() {
                    override fun areItemsTheSame(oldItem: MenuModel, newItem: MenuModel): Boolean =
                        true

                    override fun areContentsTheSame(
                        oldItem: MenuModel,
                        newItem: MenuModel
                    ): Boolean =
                        oldItem.hashCode() == newItem.hashCode()
                })
                setOnItemChildClickListener{ adapter: BaseQuickAdapter<*, *>, _: View?, position: Int ->
                    logd("selected position: $position")
                    with(data[position]){
                        val alreadySelected = isSelected

                        if (!alreadySelected && data[position].itemType == MenuModel.TYPE_DATA) {
                            updateIsSelected(groupId, itemId).run{
                                notifyItemRangeChanged(start, last - start + 1)
                            }

                            itemOn(title)
                        }
                    }


                    binding.drawer.closeDrawers()
                }
            }
        }


    }


    private fun itemOn(name: String){
        //change effect, drawer pattern, content
        when(name){
            //FIXME
            "TV Guide" -> {
                // FragTVSchedule1().navi(supportFragmentManager, R.id.container, false)
                /**
                 * FragTVSchedule1  頻道和節目表鑲嵌編排, 含有標準線
                 * FragTVSchedule2  頻道欄和節目表, 沒有標準線, NestSV移動會讓頻爆欄跟手
                 * FragTVSchedule3  頻道欄和節目表, 沒有標準線, 全部捲動都不跟手
                 */
                FragTVSchedule3().navi(supportFragmentManager, R.id.container, false)
            }
            "Channel Search" -> {

            }
            "Slide bar" -> {
            }
            "Gesture" -> {
            }
            "Overlap" -> {
                /**
                 * 不實作listener
                 * 這樣就能打開/關閉 drawer
                 *
                 */
                binding.drawer.resetToDefault()
            }
            "Shift" -> {
                with(binding.drawer){
                    resetToDefault()
                    setScrimColor(Color.TRANSPARENT)
                    drawerListenr = object : DrawerLayout.DrawerListener {
                        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                            /**
                             * open drawer : slideOffset(0->1)
                             * close drawer: slideOffset(1->0)
                             */
                            println("slideOffset: $slideOffset")
                            binding.container.translationX = binding.drawerSheet.navigationView.width * slideOffset
                        }

                        override fun onDrawerOpened(drawerView: View) {}

                        override fun onDrawerClosed(drawerView: View) {}

                        override fun onDrawerStateChanged(newState: Int) {}
                    }
                    addDrawerListener(drawerListenr!!)
                }

            }
            "Scale" -> {
                with(binding.drawer){
                    resetToDefault()
                    setScrimColor(Color.TRANSPARENT)
                    var scaleFactor = 5f
                    drawerListenr = object : DrawerLayout.DrawerListener {
                        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                            /**
                             * open drawer : slideOffset(0->1)
                             * close drawer: slideOffset(1->0)
                             */
                            println("slideOffset: $slideOffset")
                            binding.container.translationX =
                                binding.drawerSheet.navigationView.width * slideOffset
                            /**
                             * 這樣會把content 都移到畫面外
                             * 需要有一個factor才行
                             *
                              binding.container.scaleX = 1-slideOffset
                              binding.container.scaleY = 1-slideOffset
                             *
                             */
                            //PATCH: 如果要沒有navigationView 的陰影，可能要將NavigationView 換成LinearLayout
                            binding.container.scaleX = 1-(slideOffset/scaleFactor)
                            binding.container.scaleY = 1-(slideOffset/scaleFactor)
                        }

                        override fun onDrawerOpened(drawerView: View) {}

                        override fun onDrawerClosed(drawerView: View) {}

                        override fun onDrawerStateChanged(newState: Int) {}
                    }
                    addDrawerListener(drawerListenr!!)
                }

            }
        }
    }

    private fun DrawerLayout.resetToDefault(){
        if (drawerListenr != null) this.removeDrawerListener(drawerListenr!!)
        //當drawer右移，是否有陰影
        this.setScrimColor(0x99000000.toInt())//DrawerLayout.DEFAULT_SCRIM_COLOR = 0x99000000
        //Shift effect: 因為是點開drawer,content已位移,所以content 位移也要歸零
        binding.container.translationX = 0f
        //Scale effect:
        binding.container.scaleX = 1f
        binding.container.scaleY = 1f
    }


    inner class MenuAdapter(list: List<MenuModel>) : BaseMultiItemQuickAdapter<MenuModel, BaseViewHolder>(
        list.toMutableList()
    ){
        private val TAG = MenuAdapter::class.java.simpleName
        init {
            addItemType(MenuModel.TYPE_HEADER, R.layout.single_menu_header)
            addItemType(MenuModel.TYPE_DATA, R.layout.single_menu_data)
            addChildClickViewIds(R.id.tVtitle)
        }


        override fun convert(holder: BaseViewHolder, item: MenuModel) {

            when(item.itemType){
                MenuModel.TYPE_HEADER -> {
                    holder.getView<TextView>(R.id.tVtitle).text = item.title
                }
                MenuModel.TYPE_DATA -> {
                    with(holder.getView<TextView>(R.id.tVtitle)) {
                        text = item.title
                        isSelected = item.isSelected
                        setBackgroundColor(
                            Color.parseColor(
                                if (isSelected) "#575647"
                                else "#c4c4c4"
                            )
                        )
                    }
                }
            }
        }

        //改寫 listener
        override fun setOnItemChildClick(v: View, position: Int) {
            if (data[position].itemType == MenuModel.TYPE_DATA){
                super.setOnItemChildClick(v, position)
            }
        }

        /**
         * @return start-end range for notifyChange
         * NOTE: 操作這方法途中不要牽扯itemInsert, itemDeleted
         */
        fun updateIsSelected(groupId: Int, itemId: Int) : IntRange{
            println("groupId=$groupId, itemId=$itemId")
            var position0 = -1
            var position1 = -1

            data.forEachIndexed{ index, it ->
                println("===> groupId=${it.groupId}, itemId=${it.itemId}, isSelected=${it.isSelected}")
                if (it.groupId == groupId && it.itemType == MenuModel.TYPE_DATA){
                    it.isSelected = when{
                        it.isSelected -> {//true
                            position0 = index
                            false
                        }
                        it.itemId == itemId ->{
                            position1 = index
                            true
                        }
                        else -> false
                    }
                }

                println("---$position0, $position1")
                //start & end 非初始值 -> 找出位置
                if (position0 != -1 && position1 != -1){
                    Log.d(TAG, "found start-end pointer!")
                    return min(position0,position1)..max(position0,position1)
                }
            }
            Log.e(TAG, "updateIsSelected: Not Find the Selected Item")
            return 0..1
        }
    }
}