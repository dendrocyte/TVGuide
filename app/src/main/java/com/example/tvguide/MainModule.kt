package com.example.tvguide

import com.example.tvguide.vm.LiveShareViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Created by luyiling on 2021/8/6
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
val mainModule = module {
    //不需要由build varient: rx & coroutine 建立的DI
    viewModel { LiveShareViewModel(get()) }
}