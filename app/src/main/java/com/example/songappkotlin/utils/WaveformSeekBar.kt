package com.example.songappkotlin.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatSeekBar

class WaveformSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private val paintCompleted = Paint().apply {
        color = Color.DKGRAY // Màu của phần đã hoàn thành
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintRemaining = Paint().apply {
        color = Color.GRAY // Màu của phần chưa hoàn thành
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var waveform: FloatArray? = null
    private val cornerRadius: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics
    )
    private val barSpacing: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 2f, context.resources.displayMetrics
    )

    init {
//        thumb = null
        progressDrawable = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        waveform?.let { waveformArray ->
            if (waveformArray.isNotEmpty()) {
                val barWidth = (width - (waveformArray.size - 1) * barSpacing) / waveformArray.size
                val centerY = height / 2f
                val progressIndex = ((progress.toFloat() / max) * waveformArray.size).toInt()

                for (i in waveformArray.indices) {
                    val barHeight = waveformArray[i] * height
                    val left = i * (barWidth + barSpacing)
                    val top = centerY - barHeight / 2
                    val right = left + barWidth
                    val bottom = centerY + barHeight / 2
                    val paint = if (i <= progressIndex) paintCompleted else paintRemaining
                    canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paint)
                }
            }
        }
    }

    fun setWaveform(waveform: FloatArray) {
        this.waveform = waveform
        invalidate()
    }
}
