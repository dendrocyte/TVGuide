package com.example.tvguide

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.io.Serializable

/**
 * Created by luyiling on 2020/9/4
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
fun FragmentActivity.navi(@IdRes container : Int,
                          frag : Fragment,
                          allowback : Boolean = false,
                          tag: String? = null){
    logd("destFrag: $frag")
    //replace(container, frag): when screen orientation, it will gen a frag, if use add, it will overlay
    supportFragmentManager
        .beginTransaction()
        .replace(container, frag, tag)
        .also { if (allowback) it.addToBackStack(null) }
        .commit()
}
fun FragmentActivity.navi(dialogFrag : DialogFragment){
    logd("destFrag: $this")
    dialogFrag.show(supportFragmentManager, "Dialog")
}
inline fun <reified T: Any>FragmentActivity.startService(){
    startService(intentFor<T>())
}



fun Fragment.navi(childFragmentManager : FragmentManager,
                  @IdRes container : Int,
                  allowback : Boolean = false,
                  tag: String? = null){
    childFragmentManager
        .beginTransaction()
        .replace(container, this)
        .disallowAddToBackStack()
        .commit()
}
fun Fragment.navi(dialogFrag : DialogFragment){
    logd("destFrag: $this")
    dialogFrag.show(childFragmentManager, "Dialog")
}
inline fun <reified T: Any>Fragment.intentFor(vararg pair: Pair<String, Any?>): Intent{
    return requireContext().intentFor<T>(*pair)
}
inline fun <reified T: Any>Fragment.startService(){
    this.requireContext().run {
        startService(intentFor<T>())
    }
}
inline fun <reified T: Any>Fragment.startService(vararg pair: Pair<String, Any?>){
    this.requireContext().run {
        startService(intentFor<T>(*pair))
    }
}



fun consume(navi: () -> Unit): Boolean{
    navi()
    return true
}



inline fun <reified T : Any> Context.intentFor(vararg pair: Pair<String, Any?>) : Intent{
    val intent = this.intentFor<T>()
    pair.forEach {
        val value = it.second
        when (value) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
    }
    return intent
}
inline fun <reified T : Any> Context.intentFor() : Intent = Intent(this, T::class.java)
fun Context.alert(
    message: CharSequence,
    title: CharSequence? = null,
    init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> {
    return AndroidAlertBuilder(this).apply {
        if (title != null) {
            this.title = title
        }
        this.message = message
        if (init != null) init()
    }
}



fun Any.logd(msg: String) = Log.d(this::class.java.simpleName, msg)
fun Any.loge(msg: String, e:Throwable? = null) = Log.e(this::class.java.simpleName, "msg: $msg", e)
fun Any.logw(msg : String) = Log.w(this::class.java.simpleName, msg)




////////////////////// 全螢幕 ////////////////////////////////////

fun Activity.changeWindowStatusBar(color: Int = Color.parseColor("#c45a5a")){
    /**
     * @version > Android 11 (R)
     * windowInsetsController instead
     * @version < Android 11 (R)
     * systemUiVisibility
     *
     * statusBarColor 無法顯示刷透明度的顏色，即使colorString有含透明度的值，也無法
     */
    window.statusBarColor = color
}



/////////////////////// 初始化drawer /////////////////////////////

