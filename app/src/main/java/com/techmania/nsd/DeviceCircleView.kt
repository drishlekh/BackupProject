package com.techmania.nsd
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
class DeviceCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val devicePaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var devices = listOf<String>()

    fun setDevices(devices: List<String>) {
        this.devices = devices
        invalidate() // Redraw the view with new data
    }

//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // Calculate the center and radius of the circle
//        val centerX = width / 2f
//        val centerY = height / 2f
//        val radius = (Math.min(centerX, centerY) * 0.8).toFloat()
//
//        // Draw the circle
//        canvas.drawCircle(centerX, centerY, radius, paint)
//
//        // Draw the devices as dots around the circle
//        if (devices.isNotEmpty()) {
//            val angleStep = 360.0 / devices.size
//            devices.forEachIndexed { index, _ ->
//                val angle = Math.toRadians(angleStep * index)
//                val deviceX = centerX + (radius * Math.cos(angle)).toFloat()
//                val deviceY = centerY + (radius * Math.sin(angle)).toFloat()
//                canvas.drawCircle(deviceX, deviceY, 20f, devicePaint)
//            }
//        }
//    }
@SuppressLint("DrawAllocation")
override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    // Calculate the center and radius of the circle
    val centerX = width / 2f
    val centerY = height / 2f
    val radius = (Math.min(centerX, centerY) * 0.8).toFloat()

    // Draw the circle
    canvas.drawCircle(centerX, centerY, radius, paint)

    // Draw the devices as dots around the circle
    if (devices.isNotEmpty()) {
        val angleStep = 360.0 / devices.size
        devices.forEachIndexed { index, deviceName ->
            val angle = Math.toRadians(angleStep * index)
            val deviceX = centerX + (radius * Math.cos(angle)).toFloat()
            val deviceY = centerY + (radius * Math.sin(angle)).toFloat()

            // Draw the red dot
            canvas.drawCircle(deviceX, deviceY, 20f, devicePaint)

            // Split device name into multiple lines if it's too long
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 30f
            }
            val textLines = splitStringIntoLines(deviceName, textPaint, radius * 0.8)

            // Draw each line of the device name next to the red dot
            val lineHeight = textPaint.fontSpacing
            textLines.forEachIndexed { lineIndex, line ->
                canvas.drawText(
                    line,
                    deviceX - textPaint.measureText(line) / 2,
                    deviceY + lineHeight * lineIndex - (textLines.size - 1) * lineHeight / 2,
                    textPaint
                )
            }
        }
    }
}

    private fun splitStringIntoLines(text: String, paint: Paint, maxWidth: Double): List<String> {
        val result = mutableListOf<String>()
        var startIndex = 0
        var endIndex = 1
        while (endIndex <= text.length) {
            val substring = text.substring(startIndex, endIndex)
            if (paint.measureText(substring) > maxWidth && endIndex > startIndex + 1) {
                result.add(text.substring(startIndex, endIndex - 1))
                startIndex = endIndex - 1
            }
            endIndex++
        }
        if (startIndex < text.length) {
            result.add(text.substring(startIndex))
        }
        return result
    }


}


//
//package com.techmania.nsd
//
//import android.content.Context
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.util.AttributeSet
//import android.view.View
//
//class DeviceCircleView @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr) {
//
//    private val paint = Paint().apply {
//        color = Color.RED
//        isAntiAlias = true
//    }
//    private val textPaint = Paint().apply {
//        color = Color.BLACK
//        textSize = 40f
//        isAntiAlias = true
//    }
//
//    private var deviceData = listOf<Pair<Float, Float>>()
//
//    fun setDeviceData(data: List<Pair<Float, Float>>) {
//        this.deviceData = data
//        invalidate()
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        val radius = (width.coerceAtMost(height) / 2 * 0.8).toFloat()
//        val centerX = width / 2
//        val centerY = height / 2
//
//        val dotRadius = 20f
//
//        for ((x, y) in deviceData) {
//            // Draw the dot
//            canvas.drawCircle(x, y, dotRadius, paint)
//
//            // Draw the device name
//            val textX = x + dotRadius + 10
//            val textY = y
//            canvas.drawText("Device", textX, textY, textPaint)
//        }
//    }
//}
