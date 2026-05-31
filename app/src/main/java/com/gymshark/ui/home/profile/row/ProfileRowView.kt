package com.gymshark.ui.home.profile.row

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.gymshark.R

class ProfileRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvTitle: TextView
    private val tvValue: TextView

    init {
        orientation = HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(12))
        background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_profile_glass_soft)
        isClickable = true
        isFocusable = true

        LayoutInflater.from(context).inflate(R.layout.view_profile_row, this, true)

        tvTitle = findViewById(R.id.tvTitle)
        tvValue = findViewById(R.id.tvValue)
    }


    fun setValue(text: String) {
        tvValue.text = text
    }
    fun bind(model: ProfileRowModel) {
        tvTitle.text = model.title
        tvValue.text = model.value
        isEnabled = model.enabled
        alpha = if (model.enabled) 1f else 0.55f
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    fun flash() {
        animate().alpha(0.5f).setDuration(80).withEndAction {
            animate().alpha(1f).duration = 120
        }
    }

}
