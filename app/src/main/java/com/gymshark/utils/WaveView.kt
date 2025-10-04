package com.gymshark.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class WaveView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    data class Harmonic(
        val amplitude: Float,
        val frequency: Float,
        val phase: Float
    )

    private val harmonics = mutableListOf<Harmonic>()

    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL   // тепер малюємо заповнення
        color = Color.parseColor("#33FF0000") // напівпрозорий червоний
    }

    private val path = Path()

    var baseLine = 300f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()
        path.moveTo(0f, baseLine)

        val step = 5f
        for (x in 0..width step step.toInt()) {
            var y = baseLine
            harmonics.forEach { h ->
                y += h.amplitude * sin(
                    (x / width.toFloat()) * h.frequency * Math.PI * 2 + h.phase
                ).toFloat()
            }
            path.lineTo(x.toFloat(), y)
        }

        // закриваємо фігуру вниз до низу екрана (або до baseline)
        path.lineTo(width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.close()

        canvas.drawPath(path, wavePaint)
    }

    fun setHarmonics(list: List<Harmonic>) {
        harmonics.clear()
        harmonics.addAll(list)
        invalidate()
    }
}
