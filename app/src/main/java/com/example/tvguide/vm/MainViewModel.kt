package com.example.tvguide.vm

import androidx.lifecycle.ViewModel
import com.example.tvguide.model.MenuModel

/**
 * Created by luyiling on 2021/11/12
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class MainViewModel : ViewModel() {

    /*get menu list*/
    val menuList : List<MenuModel> = listOf(
            //content
            MenuModel(0, -1,false, "Content", MenuModel.TYPE_HEADER),
            MenuModel(0, 0,true, "TV Guide", MenuModel.TYPE_DATA),
            MenuModel(0, 1,false, "Channel Search", MenuModel.TYPE_DATA),
            //open drawer
            MenuModel(1, -1,false, "Open Drawer", MenuModel.TYPE_HEADER),
            MenuModel(1, 0,true, "Gesture", MenuModel.TYPE_DATA),
            MenuModel(1, 1,false, "Slide Bar", MenuModel.TYPE_DATA),
            //drawer effect
            MenuModel(2, -1,false, "Drawer Effect", MenuModel.TYPE_HEADER),
            MenuModel(2, 0,true, "Overlap", MenuModel.TYPE_DATA),
            MenuModel(2, 1,false, "Shift", MenuModel.TYPE_DATA),
            MenuModel(2, 2,false, "Scale", MenuModel.TYPE_DATA)
    )
}