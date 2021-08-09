package com.example.tvguide

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.lang.Exception

/**
 * Created by luyiling on 2021/4/14
 * 2022 Feb Jcenter shut down, Anko is setteled in JCenter and obsteled
 * => copy from Anko
 * Modified by
 *
 * TODO:
 * Description:
 *
 * @params
 * @params
 */
interface AlertBuilder<out D : DialogInterface> {
    val ctx: Context

    var title: CharSequence
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get
    var titleResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get
    var message: CharSequence
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get
    var messageResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get
    var icon: Drawable
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get

    @setparam:DrawableRes
    var iconResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get

    var isCancelable: Boolean
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get

    fun onCancelled(handler: (dialog: DialogInterface) -> Unit)

    fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean)

    fun positiveButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit)
    fun positiveButton(@StringRes buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit)

    fun negativeButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit)
    fun negativeButton(@StringRes buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit)

    fun neutralPressed(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit)
    fun neutralPressed(@StringRes buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit)

    fun items(items: List<CharSequence>, onItemSelected: (dialog: DialogInterface, index: Int) -> Unit)
    fun <T> items(items: List<T>, onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit)

    fun build(): D
    fun show(): D
}

internal class AndroidAlertBuilder(override val ctx: Context) :
    AlertBuilder<AlertDialog> {
    private val builder = AlertDialog.Builder(ctx)

    override var title: CharSequence
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setTitle(value) }

    override var titleResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setTitle(value) }

    override var message: CharSequence
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setMessage(value) }

    override var messageResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setMessage(value) }

    override var icon: Drawable
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setIcon(value) }

    override var iconResource: Int
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setIcon(value) }

    override var isCancelable: Boolean
        @Deprecated(NO_GETTER, level = DeprecationLevel.ERROR) get() = EXP_NO_GETTER
        set(value) { builder.setCancelable(value) }

    override fun onCancelled(handler: (DialogInterface) -> Unit) {
        builder.setOnCancelListener(handler)
    }

    override fun onKeyPressed(handler: (dialog: DialogInterface, keyCode: Int, e: KeyEvent) -> Boolean) {
        builder.setOnKeyListener(handler)
    }

    override fun positiveButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun positiveButton(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setPositiveButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun negativeButton(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNegativeButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonText: String, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonText) { dialog, _ -> onClicked(dialog) }
    }

    override fun neutralPressed(buttonTextResource: Int, onClicked: (dialog: DialogInterface) -> Unit) {
        builder.setNeutralButton(buttonTextResource) { dialog, _ -> onClicked(dialog) }
    }

    override fun items(items: List<CharSequence>, onItemSelected: (dialog: DialogInterface, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, which)
        }
    }

    override fun <T> items(items: List<T>, onItemSelected: (dialog: DialogInterface, item: T, index: Int) -> Unit) {
        builder.setItems(Array(items.size) { i -> items[i].toString() }) { dialog, which ->
            onItemSelected(dialog, items[which], which)
        }
    }

    override fun build(): AlertDialog = builder.create()

    override fun show(): AlertDialog = builder.show()
}


const val NO_GETTER = "Property does not have a getter"
val EXP_NO_GETTER : Nothing = throw Exception(NO_GETTER)


inline fun AlertBuilder<*>.okButton(noinline handler: (dialog: DialogInterface) -> Unit) =
    positiveButton(android.R.string.ok, handler)

inline fun AlertBuilder<*>.cancelButton(noinline handler: (dialog: DialogInterface) -> Unit) =
    negativeButton(android.R.string.cancel, handler)

inline fun AlertBuilder<*>.yesButton(noinline handler: (dialog: DialogInterface) -> Unit) =
    positiveButton(android.R.string.yes, handler)

inline fun AlertBuilder<*>.noButton(noinline handler: (dialog: DialogInterface) -> Unit) =
    negativeButton(android.R.string.no, handler)