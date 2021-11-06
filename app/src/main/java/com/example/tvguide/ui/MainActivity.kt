package com.example.tvguide.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tvguide.R
import com.example.tvguide.navi

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //if use binding, this line is not needed to write
        setContentView(R.layout.activity_main)

       // FragTVSchedule1().navi(supportFragmentManager, R.id.container, false)
        /**
         * FragTVSchedule1  頻道和節目表鑲嵌編排, 含有標準線
         * FragTVSchedule2  頻道欄和節目表, 沒有標準線, NestSV移動會讓頻爆欄跟手
         * FragTVSchedule3  頻道欄和節目表, 沒有標準線, 全部捲動都不跟手
         */
        FragTVSchedule2().navi(supportFragmentManager, R.id.container, false)

    }
}