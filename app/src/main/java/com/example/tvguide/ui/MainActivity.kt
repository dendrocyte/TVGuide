package com.example.tvguide.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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
import com.example.tvguide.loge
import com.example.tvguide.model.MenuModel
import com.example.tvguide.navi
import com.example.tvguide.vm.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val viewModel : MainViewModel by viewModel()
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
            /**
             * 不實作listener
             * 這樣就能打開/關閉 drawer
             *
             */
            addDrawerListener(object : DrawerLayout.DrawerListener{
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    //DO?
                }

                override fun onDrawerOpened(drawerView: View) {
                    //DO?
                }

                override fun onDrawerClosed(drawerView: View) {
                    //DO?
                }

                override fun onDrawerStateChanged(newState: Int) {
                    //DO?
                }
            })

            for(item in viewModel.menuList){
                if (item.isSelected) itemOn(item.title)
            }
        }



        with(binding.drawer.findViewById<RecyclerView>(R.id.recyclerDrawer)){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            logd("drawer menu size: ${viewModel.menuList.size}")
            adapter = MenuAdapter(viewModel.menuList).apply {
                setDiffCallback(object : DiffUtil.ItemCallback<MenuModel>(){
                    override fun areItemsTheSame(oldItem: MenuModel, newItem: MenuModel): Boolean = true
                    override fun areContentsTheSame(oldItem: MenuModel, newItem: MenuModel): Boolean =
                            oldItem.hashCode() == newItem.hashCode()
                })
                setOnItemChildClickListener{ adapter: BaseQuickAdapter<*, *>, _: View?, position: Int ->
                    logd("selected position: $position")
                    with(data[position]){
                        val alreadySelected = isSelected

                        if (!alreadySelected) {
                            updateIsSelected(groupId, itemId).run{
                                notifyItemRangeChanged(start, last-start +1)
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
            "Channel Search" -> {}
            "Slide bar" -> {}
            "Gesture" -> {}
            "Overlap" -> {}
            "Shift" -> {}
            "Scale" -> {}
        }
    }




    inner class MenuAdapter(list : List<MenuModel>) : BaseMultiItemQuickAdapter<MenuModel,BaseViewHolder>(
            list.toMutableList()
    ){
        private val TAG = MenuAdapter::class.java.simpleName
        init {
            addItemType(MenuModel.TYPE_HEADER, R.layout.single_menu_header)
            addItemType(MenuModel.TYPE_DATA, R.layout.single_menu_data)
        }


        override fun convert(holder: BaseViewHolder, item: MenuModel) {

            when(item.itemType){
                MenuModel.TYPE_HEADER -> {
                    holder.getView<TextView>(R.id.tVtitle).text = item.title
                }
                MenuModel.TYPE_DATA -> {
                    with(holder.getView<TextView>(R.id.tVtitle)){
                        text = item.title
                        isSelected = item.isSelected
                        setBackgroundColor(Color.parseColor(
                                if (isSelected) "#575647"
                                else "#c4c4c4"
                        ))
                    }
                }
            }
        }



        /**
         * @return start-end range for notifyChange
         * NOTE: 操作這方法途中不要牽扯itemInsert, itemDeleted
         */
        fun updateIsSelected(groupId : Int, itemId: Int) : IntRange{
            var startPosition = -1
            var endPosition = -1
            data.forEachIndexed{ index, it ->
                println("check: $index")

                if (it.groupId == groupId && it.itemType == MenuModel.TYPE_DATA){
                    it.isSelected = when(it.isSelected){
                        true -> {
                            startPosition = index
                            false
                        }
                        it.itemId == itemId ->{
                            endPosition = index
                            true
                        }
                        else -> false
                    }
                }

                //start & end 非初始值 -> 找出位置
                if (startPosition != -1 && endPosition != -1)
                    return startPosition..endPosition
                //FIXME: change data's isSelected
            }
            Log.e(TAG, "updateIsSelected: Not Find the Selected Item" )
            return 0..1
        }
    }
}