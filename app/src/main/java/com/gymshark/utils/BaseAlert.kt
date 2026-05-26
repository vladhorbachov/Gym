package com.gymshark.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BaseAlert(private val context: Context) {

    private var title: String? = null
    private var message: String? = null
    private var positiveText: String = "OK"
    private var onPositive: (() -> Unit)? = null
    private var negativeText: String? = "Cancel"
    private var onNegative: (() -> Unit)? = null
    private var neutralText: String? = null
    private var onNeutral: (() -> Unit)? = null

    fun title(text: String) = apply { title = text }
    fun message(text: String) = apply { message = text }

    fun positiveButton(text: String, action: () -> Unit) = apply {
        positiveText = text
        onPositive = action
    }

    fun negativeButton(text: String, action: (() -> Unit)? = null) = apply {
        negativeText = text
        onNegative = action
    }

    fun neutralButton(text: String, action: (() -> Unit)? = null) = apply {
        neutralText = text
        onNeutral = action
    }

    fun show() {
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                onPositive?.invoke()
                dialog.dismiss()
            }

        negativeText?.let { text ->
            builder.setNegativeButton(text) { dialog, _ ->
                onNegative?.invoke()
                dialog.dismiss()
            }
        }

        neutralText?.let { text ->
            builder.setNeutralButton(text) { dialog, _ ->
                onNeutral?.invoke()
                dialog.dismiss()
            }
        }

        builder.show()
    }
}
