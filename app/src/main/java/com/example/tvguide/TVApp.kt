package com.example.tvguide

import android.app.Application
import appModule
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import remoteModule

/**
 * Created by luyiling on 2021/8/5
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
class TVApp : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            koin.loadModules(listOf(remoteModule, appModule,mainModule ))
        }
    }
}

