package com.gymshark.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.gymshark.R
import kotlin.math.abs
import kotlin.math.max

class MorphingBottomNavView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun interface OnTabSelectedListener {
        fun onTabSelected(index: Int)
    }

    // Change these colors to restyle the component.
    @ColorInt private val selectedColor = ContextCompat.getColor(context, R.color.trainingAction)
    @ColorInt private val inactiveColor = Color.WHITE
    @ColorInt private val backgroundColor = Color.TRANSPARENT
    @ColorInt private val borderColor = ContextCompat.getColor(context, R.color.strokeSubtle)

    // Change this list to add/remove tabs. Keep HomeActivity mapping in sync.
    private val tabs = listOf(
        IconSpec.home(),
        IconSpec.stats(),
        IconSpec.meal(),
        IconSpec.profile()
    )

    // Change this value to tune animation speed.
    private val animationDurationMs = 340L

    private val interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    private val tabCenters = FloatArray(tabs.size)
    private val backgroundRect = RectF()
    private val tempRect = RectF()

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundColor
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = borderColor
    }

    private var listener: OnTabSelectedListener? = null
    private var animator: ValueAnimator? = null
    private var selectedIndex = 0
    private var fromIndex = 0
    private var targetIndex = 0
    private var progress = 1f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        isClickable = true
        isFocusable = true
    }

    fun setOnTabSelectedListener(listener: OnTabSelectedListener?) {
        this.listener = listener
    }

    // Use animate = false when syncing from NavController destination changes.
    fun setSelectedTab(index: Int, animate: Boolean = true) {
        val safeIndex = index.coerceIn(0, tabs.lastIndex)
        if (safeIndex == selectedIndex) return

        animator?.cancel()
        fromIndex = if (progress < 1f) targetIndex else selectedIndex
        targetIndex = safeIndex
        selectedIndex = safeIndex

        if (!animate) {
            progress = 1f
            invalidate()
            return
        }

        progress = 0f
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animationDurationMs
            interpolator = this@MorphingBottomNavView.interpolator
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calculateTabCenters()
        drawBackground(canvas)

        for (index in tabs.indices) {
            val hideTargetWhileAssembling = progress < 1f && index == targetIndex
            if (!hideTargetWhileAssembling) {
                val color = if (index == selectedIndex) selectedColor else inactiveColor
                drawIcon(canvas, tabs[index], tabCenters[index], height / 2f, color, 1f, 1f)
            }
        }

        if (progress < 1f) {
            drawMorph(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val index = findTabIndex(event.x)
            if (index != selectedIndex) {
                // Selection behavior lives here; screen switching is delegated via callback.
                setSelectedTab(index, animate = true)
                listener?.onTabSelected(index)
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun drawBackground(canvas: Canvas) {
        val inset = dp(1f)
        backgroundRect.set(inset, inset, width - inset, height - inset)
        val radius = height / 2.15f
        canvas.drawRoundRect(backgroundRect, radius, radius, bgPaint)
        canvas.drawRoundRect(backgroundRect, radius, radius, borderPaint)
    }

    private fun drawMorph(canvas: Canvas) {
        val eased = progress
        val fromSpec = tabs[fromIndex]
        val targetSpec = tabs[targetIndex]
        val fromCenter = tabCenters[fromIndex]
        val targetCenter = tabCenters[targetIndex]
        val centerY = height / 2f
        val maxParts = max(fromSpec.parts.size, targetSpec.parts.size)

        for (i in 0 until maxParts) {
            val from = fromSpec.parts[i % fromSpec.parts.size]
            val target = targetSpec.parts[i % targetSpec.parts.size]

            val delay = i * 0.045f
            val local = ((eased - delay) / (1f - delay)).coerceIn(0f, 1f)
            val scatter = sinLike(local)
            val direction = if (targetCenter > fromCenter) 1f else -1f
            val centerX = lerp(fromCenter, targetCenter, local)
            val spreadX = direction * dp((i - (maxParts - 1) / 2f) * 5.8f) * scatter
            val spreadY = dp(
                when (i % 4) {
                    0 -> -28f
                    1 -> 20f
                    2 -> -15f
                    else -> 12f
                }
            ) * scatter
            val movingPart = from.explodeAndLerpTo(target, local, i, maxParts)
            val scale = lerp(0.9f, 1.18f, scatter)
            val stroke = lerp(from.strokeWidth + 0.25f, target.strokeWidth, local)
            val color = blend(selectedColor, selectedColor, local)

            drawPart(
                canvas = canvas,
                part = movingPart.copy(strokeWidth = stroke),
                centerX = centerX + spreadX,
                centerY = centerY + spreadY,
                color = color,
                alpha = lerp(0.88f, 1f, local),
                scale = scale
            )
        }

        if (eased > 0.46f) {
            val assemble = ((eased - 0.46f) / 0.54f).coerceIn(0f, 1f)
            drawAssemblingIcon(canvas, targetSpec, targetCenter, centerY, selectedColor, assemble)
        }

        drawTravelDots(canvas, fromCenter, targetCenter, centerY, eased)
    }

    private fun drawAssemblingIcon(
        canvas: Canvas,
        spec: IconSpec,
        centerX: Float,
        centerY: Float,
        @ColorInt color: Int,
        progress: Float
    ) {
        spec.parts.forEachIndexed { index, part ->
            val spread = 1f - progress
            val offsetX = dp((index - (spec.parts.size - 1) / 2f) * 3.8f) * spread
            val offsetY = dp(if (index % 2 == 0) -8f else 8f) * spread
            drawPart(
                canvas = canvas,
                part = part,
                centerX = centerX + offsetX,
                centerY = centerY + offsetY,
                color = color,
                alpha = progress * 0.75f,
                scale = lerp(0.82f, 1f, progress)
            )
        }
    }

    private fun drawTravelDots(canvas: Canvas, startX: Float, endX: Float, centerY: Float, t: Float) {
        fillPaint.color = selectedColor
        repeat(2) { index ->
            val delay = index * 0.12f
            val localT = ((t - delay) / 0.76f).coerceIn(0f, 1f)
            val alpha = (90 * (1f - abs(localT - 0.5f) * 2f).coerceIn(0f, 1f)).toInt()
            fillPaint.alpha = alpha
            canvas.drawCircle(
                lerp(startX, endX, localT),
                centerY + dp(-13f + index * 26f),
                dp(1.35f),
                fillPaint
            )
        }
        fillPaint.alpha = 255
    }

    private fun drawIcon(
        canvas: Canvas,
        spec: IconSpec,
        centerX: Float,
        centerY: Float,
        @ColorInt color: Int,
        alpha: Float,
        scale: Float
    ) {
        spec.parts.forEach { drawPart(canvas, it, centerX, centerY, color, alpha, scale) }
    }

    private fun drawPart(
        canvas: Canvas,
        part: IconPart,
        centerX: Float,
        centerY: Float,
        @ColorInt color: Int,
        alpha: Float,
        scale: Float
    ) {
        val scaledStroke = dp(part.strokeWidth) * scale
        iconPaint.color = color
        iconPaint.alpha = (255 * alpha).toInt().coerceIn(0, 255)
        iconPaint.strokeWidth = scaledStroke

        fillPaint.color = color
        fillPaint.alpha = iconPaint.alpha

        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(scale, scale)

        when (part.kind) {
            PartKind.Line -> {
                canvas.drawLine(dp(part.x1), dp(part.y1), dp(part.x2), dp(part.y2), iconPaint)
            }
            PartKind.Circle -> {
                iconPaint.style = Paint.Style.STROKE
                canvas.drawCircle(dp(part.x1), dp(part.y1), dp(part.radius), iconPaint)
            }
            PartKind.Dot -> {
                canvas.drawCircle(dp(part.x1), dp(part.y1), dp(part.radius), fillPaint)
            }
            PartKind.RoundRect -> {
                tempRect.set(
                    dp(part.x1 - part.width / 2f),
                    dp(part.y1 - part.height / 2f),
                    dp(part.x1 + part.width / 2f),
                    dp(part.y1 + part.height / 2f)
                )
                canvas.drawRoundRect(tempRect, dp(part.radius), dp(part.radius), iconPaint)
            }
            PartKind.Arc -> {
                tempRect.set(
                    dp(part.x1 - part.width / 2f),
                    dp(part.y1 - part.height / 2f),
                    dp(part.x1 + part.width / 2f),
                    dp(part.y1 + part.height / 2f)
                )
                canvas.drawArc(tempRect, part.startAngle, part.sweepAngle, false, iconPaint)
            }
        }

        canvas.restore()
        iconPaint.alpha = 255
        fillPaint.alpha = 255
    }

    private fun calculateTabCenters() {
        val itemWidth = width / tabs.size.toFloat()
        for (i in tabs.indices) {
            tabCenters[i] = itemWidth * i + itemWidth / 2f
        }
    }

    private fun findTabIndex(x: Float): Int {
        val itemWidth = width / tabs.size.toFloat()
        return (x / itemWidth).toInt().coerceIn(0, tabs.lastIndex)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun lerp(start: Float, end: Float, t: Float): Float = start + (end - start) * t

    private fun blend(@ColorInt from: Int, @ColorInt to: Int, t: Float): Int {
        val clamped = t.coerceIn(0f, 1f)
        return Color.rgb(
            lerp(Color.red(from).toFloat(), Color.red(to).toFloat(), clamped).toInt(),
            lerp(Color.green(from).toFloat(), Color.green(to).toFloat(), clamped).toInt(),
            lerp(Color.blue(from).toFloat(), Color.blue(to).toFloat(), clamped).toInt()
        )
    }

    private fun sinLike(t: Float): Float = if (t < 0.5f) t * 2f else (1f - t) * 2f

    private data class IconSpec(val parts: List<IconPart>) {
        companion object {
            // Change icon shapes here. Keep parts simple for clean morphing.
            fun home() = IconSpec(
                listOf(
                    IconPart.line(-9f, -1f, 0f, -9f),
                    IconPart.line(0f, -9f, 9f, -1f),
                    IconPart.line(-7f, 0f, -7f, 9f),
                    IconPart.line(7f, 0f, 7f, 9f),
                    IconPart.line(-7f, 9f, 7f, 9f),
                    IconPart.roundRect(0f, 5f, 5.5f, 8f, 2f, 1.7f)
                )
            )

            fun stats() = IconSpec(
                listOf(
                    IconPart.line(-10f, 9f, 10f, 9f),
                    IconPart.roundRect(-6f, 3f, 3.2f, 12f, 1.6f, 1.7f),
                    IconPart.roundRect(0f, -1f, 3.2f, 20f, 1.6f, 1.7f),
                    IconPart.roundRect(6f, -5f, 3.2f, 28f, 1.6f, 1.7f),
                    IconPart.line(-8f, 1f, -2f, -4f),
                    IconPart.line(-2f, -4f, 8f, -9f)
                )
            )

            fun meal() = IconSpec(
                listOf(
                    IconPart.circle(0f, 0f, 10f, 1.7f),
                    IconPart.arc(0f, 0f, 14f, 14f, 205f, 130f, 1.7f),
                    IconPart.line(-3f, 5f, 4f, -2f),
                    IconPart.arc(-4f, -4f, 9f, 7f, 20f, 250f, 1.6f),
                    IconPart.arc(5f, -4f, 9f, 7f, 190f, 250f, 1.6f),
                    IconPart.dot(0f, 0f, 1.5f)
                )
            )

            fun profile() = IconSpec(
                listOf(
                    IconPart.circle(0f, -6f, 4.7f, 1.7f),
                    IconPart.arc(0f, 8f, 17f, 12f, 200f, 140f, 1.8f),
                    IconPart.line(-6f, 5f, -2f, 9f),
                    IconPart.line(6f, 5f, 2f, 9f),
                    IconPart.line(-2f, 9f, 2f, 9f),
                    IconPart.dot(0f, -6f, 1.2f)
                )
            )
        }
    }

    private enum class PartKind { Line, Circle, Dot, RoundRect, Arc }

    private data class IconPart(
        val kind: PartKind,
        val x1: Float,
        val y1: Float,
        val x2: Float = x1,
        val y2: Float = y1,
        val width: Float = 0f,
        val height: Float = 0f,
        val radius: Float = 0f,
        val startAngle: Float = 0f,
        val sweepAngle: Float = 0f,
        val strokeWidth: Float = 1.8f
    ) {
        fun lerpTo(target: IconPart, t: Float): IconPart = IconPart(
            kind = if (t < 0.55f) kind else target.kind,
            x1 = lerpValue(x1, target.x1, t),
            y1 = lerpValue(y1, target.y1, t),
            x2 = lerpValue(x2, target.x2, t),
            y2 = lerpValue(y2, target.y2, t),
            width = lerpValue(width, target.width, t),
            height = lerpValue(height, target.height, t),
            radius = lerpValue(radius, target.radius, t),
            startAngle = lerpValue(startAngle, target.startAngle, t),
            sweepAngle = lerpValue(sweepAngle, target.sweepAngle, t),
            strokeWidth = lerpValue(strokeWidth, target.strokeWidth, t)
        )

        fun explodeAndLerpTo(target: IconPart, t: Float, index: Int, total: Int): IconPart {
            val eased = t * t * (3f - 2f * t)
            val burst = if (eased < 0.5f) eased * 2f else (1f - eased) * 2f
            val spreadX = (index - (total - 1) / 2f) * 2.7f * burst
            val spreadY = (if (index % 2 == 0) -5.5f else 5.5f) * burst
            val base = lerpTo(target, eased)

            return base.copy(
                x1 = base.x1 + spreadX,
                y1 = base.y1 + spreadY,
                x2 = base.x2 - spreadX * 0.45f,
                y2 = base.y2 - spreadY * 0.45f,
                width = base.width + absValue(spreadX) * 0.35f,
                height = base.height + absValue(spreadY) * 0.35f,
                radius = base.radius + burst * 0.8f
            )
        }

        companion object {
            fun line(x1: Float, y1: Float, x2: Float, y2: Float, strokeWidth: Float = 1.8f) =
                IconPart(PartKind.Line, x1, y1, x2, y2, strokeWidth = strokeWidth)

            fun circle(x: Float, y: Float, radius: Float, strokeWidth: Float = 1.8f) =
                IconPart(PartKind.Circle, x, y, radius = radius, strokeWidth = strokeWidth)

            fun dot(x: Float, y: Float, radius: Float) =
                IconPart(PartKind.Dot, x, y, radius = radius)

            fun roundRect(
                x: Float,
                y: Float,
                width: Float,
                height: Float,
                radius: Float,
                strokeWidth: Float = 1.8f
            ) = IconPart(
                PartKind.RoundRect,
                x,
                y,
                width = width,
                height = height,
                radius = radius,
                strokeWidth = strokeWidth
            )

            fun arc(
                x: Float,
                y: Float,
                width: Float,
                height: Float,
                startAngle: Float,
                sweepAngle: Float,
                strokeWidth: Float = 1.8f
            ) = IconPart(
                PartKind.Arc,
                x,
                y,
                width = width,
                height = height,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                strokeWidth = strokeWidth
            )

            private fun lerpValue(start: Float, end: Float, t: Float): Float = start + (end - start) * t

            private fun absValue(value: Float): Float = if (value < 0f) -value else value
        }
    }
}
