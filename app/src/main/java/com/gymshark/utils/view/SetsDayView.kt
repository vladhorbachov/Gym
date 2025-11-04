package com.gymshark.utils.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.gymshark.databinding.ViewSetsBinding

class SetsDayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    val binding = ViewSetsBinding.inflate(LayoutInflater.from(context), this)
    fun setDay(s: String) {
        binding.textView.text = s

    }


}