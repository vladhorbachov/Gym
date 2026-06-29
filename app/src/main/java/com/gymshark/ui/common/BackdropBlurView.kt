package com.gymshark.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView

class BackdropBlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var sourceView: View? = null
    private var blurBitmap: Bitmap? = null
    private val sourceLocation = IntArray(2)
    private val blurLocation = IntArray(2)
    private val downsample = 4
    private val blurRadius = 7

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        updateBlur()
        true
    }

    init {
        scaleType = ScaleType.FIT_XY
    }

    fun setBlurSource(source: View) {
        if (sourceView === source) return
        sourceView?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
        sourceView = source
        source.viewTreeObserver.addOnPreDrawListener(preDrawListener)
        updateBlur()
    }

    override fun onDetachedFromWindow() {
        sourceView?.viewTreeObserver?.removeOnPreDrawListener(preDrawListener)
        blurBitmap?.recycle()
        blurBitmap = null
        super.onDetachedFromWindow()
    }

    private fun updateBlur() {
        val source = sourceView ?: return
        if (width <= 0 || height <= 0 || source.width <= 0 || source.height <= 0) return

        val bitmapWidth = (width / downsample).coerceAtLeast(1)
        val bitmapHeight = (height / downsample).coerceAtLeast(1)
        val bitmap = blurBitmap
            ?.takeIf { it.width == bitmapWidth && it.height == bitmapHeight && !it.isRecycled }
            ?: Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888).also {
                blurBitmap?.recycle()
                blurBitmap = it
                setImageBitmap(it)
            }

        source.getLocationInWindow(sourceLocation)
        getLocationInWindow(blurLocation)

        bitmap.eraseColor(0x00000000)
        val canvas = Canvas(bitmap)
        canvas.scale(1f / downsample, 1f / downsample)
        canvas.translate(
            (sourceLocation[0] - blurLocation[0]).toFloat(),
            (sourceLocation[1] - blurLocation[1]).toFloat()
        )
        source.draw(canvas)

        boxBlur(bitmap, blurRadius)
    }

    private fun boxBlur(bitmap: Bitmap, radius: Int) {
        if (radius < 1) return

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        val temp = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            var a = 0
            var r = 0
            var g = 0
            var b = 0
            val row = y * width

            for (i in -radius..radius) {
                val pixel = pixels[row + i.coerceIn(0, width - 1)]
                a += pixel ushr 24
                r += pixel shr 16 and 0xff
                g += pixel shr 8 and 0xff
                b += pixel and 0xff
            }

            for (x in 0 until width) {
                val count = radius * 2 + 1
                temp[row + x] =
                    (a / count shl 24) or
                    (r / count shl 16) or
                    (g / count shl 8) or
                    (b / count)

                val removeX = (x - radius).coerceIn(0, width - 1)
                val addX = (x + radius + 1).coerceIn(0, width - 1)
                val remove = pixels[row + removeX]
                val add = pixels[row + addX]

                a += (add ushr 24) - (remove ushr 24)
                r += (add shr 16 and 0xff) - (remove shr 16 and 0xff)
                g += (add shr 8 and 0xff) - (remove shr 8 and 0xff)
                b += (add and 0xff) - (remove and 0xff)
            }
        }

        for (x in 0 until width) {
            var a = 0
            var r = 0
            var g = 0
            var b = 0

            for (i in -radius..radius) {
                val pixel = temp[i.coerceIn(0, height - 1) * width + x]
                a += pixel ushr 24
                r += pixel shr 16 and 0xff
                g += pixel shr 8 and 0xff
                b += pixel and 0xff
            }

            for (y in 0 until height) {
                val count = radius * 2 + 1
                pixels[y * width + x] =
                    (a / count shl 24) or
                    (r / count shl 16) or
                    (g / count shl 8) or
                    (b / count)

                val removeY = (y - radius).coerceIn(0, height - 1)
                val addY = (y + radius + 1).coerceIn(0, height - 1)
                val remove = temp[removeY * width + x]
                val add = temp[addY * width + x]

                a += (add ushr 24) - (remove ushr 24)
                r += (add shr 16 and 0xff) - (remove shr 16 and 0xff)
                g += (add shr 8 and 0xff) - (remove shr 8 and 0xff)
                b += (add and 0xff) - (remove and 0xff)
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }
}
