package com.techmania.nsd




// DeviceCircleView.kt
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
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

    private var devices = mutableListOf<String>()
    private var selectedDevices = mutableSetOf<Int>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (Math.min(centerX, centerY) * 0.8).toFloat()
        canvas.drawCircle(centerX, centerY, radius, paint)


        val angleStep = 360.0 / devices.size
        devices.forEachIndexed { index, _ ->
            val angle = Math.toRadians(angleStep * index)
            val deviceX = centerX + (radius * Math.cos(angle)).toFloat()
            val deviceY = centerY + (radius * Math.sin(angle)).toFloat()
            val deviceRadius = 20f
            canvas.drawCircle(deviceX, deviceY, deviceRadius, devicePaint)
            if (index in selectedDevices) {

                val indicatorRadius = deviceRadius * 1.5f
                canvas.drawCircle(deviceX, deviceY, indicatorRadius, paint)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                val centerX = width / 2f
                val centerY = height / 2f
                val radius = (Math.min(centerX, centerY) * 0.8).toFloat()
                val touchX = event.x
                val touchY = event.y
                val angle = Math.atan2((touchY - centerY).toDouble(), (touchX - centerX).toDouble())
                val distance = Math.sqrt(Math.pow((touchX - centerX).toDouble(), 2.0) + Math.pow((touchY - centerY).toDouble(), 2.0))
                val selectedDeviceIndex = ((Math.toDegrees(angle) + 360) % 360 / (360.0 / devices.size)).toInt()
                if (distance <= radius) {

                    if (selectedDevices.contains(selectedDeviceIndex)) {
                        selectedDevices.remove(selectedDeviceIndex)
                    } else {
                        selectedDevices.add(selectedDeviceIndex)
                    }
                    invalidate()
                }
            }
        }
        return true
    }

    fun setDevices(devices: List<String>) {
        this.devices = devices.toMutableList()
        selectedDevices.clear()
        invalidate()
    }
}



