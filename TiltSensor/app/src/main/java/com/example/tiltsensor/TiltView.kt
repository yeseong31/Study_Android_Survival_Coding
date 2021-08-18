package com.example.tiltsensor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.SensorEvent
import android.view.View

class TiltView(context: Context?) : View(context) {

    private val greenPaint: Paint = Paint()
    private val blackPaint: Paint = Paint()
    private var cx: Float = 0f
    private var cy: Float = 0f
    private var xCoord: Float = 0f
    private var yCoord: Float = 0f

    // 초기화
    init {
        greenPaint.color = Color.GREEN
        blackPaint.style = Paint.Style.STROKE
        // blackPaint.color = Color.WHITE
    }
    
    // 뷰의 중점 좌표를 구하는 메서드
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        
        // 중앙에 길이가 40인 직선 두 개를 교차하여 십자선을 표현
        canvas?.drawLine(cx, cy -20f, cx, cy + 20f, blackPaint)
        canvas?.drawLine(cx -20f, cy, cx + 20f, cy, blackPaint)
        // 외부 검은 테두리의 원 그리기
        canvas?.drawCircle(cx, cy, 100f, blackPaint)
        // 내부의 녹색 원 그리기
        canvas?.drawCircle(xCoord + cx, yCoord + cy, 100f, greenPaint)
    }

    // 센서 값을 받는 메서드
    fun onSensorEvent(event: SensorEvent) {
        // 화면을 가로로 돌렸으므로 x축과 y축을 서로 바꿈
        yCoord = event.values[0] * 20
        xCoord = event.values[1] * 20
        invalidate()
    }
}