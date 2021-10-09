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
        FragTVSchedule2().navi(supportFragmentManager, R.id.container, false)

    }
}