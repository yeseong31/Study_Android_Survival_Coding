package com.example.tiltsensor

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.content.getSystemService

class MainActivity : AppCompatActivity(), SensorEventListener {

    // 센서 매니저에 대한 참조는 얻어야 함
    private val sensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private lateinit var tiltView: TiltView

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면이 가로 모드로 고정되게 하기(슈퍼클래스의 생성자 호출 이전에 수행)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // 화면이 꺼지지 않게 하기(슈퍼클래스의 생성자 호출 이전에 수행)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tiltView = TiltView(this)
        setContentView(tiltView)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // 센서값이 변경되면 호출됨
        // values[0]: x축 값... 위로 기울이면 -10 ~ 0, 아래로 기울이면 0 ~ 10
        // values[1]: y축 값... 왼쪽으로 기울이면 -10 ~ 0, 오른쪽으로 기울이면 0 ~ 10
        // values[2]: z축 값... 미사용
        event?.let {
            Log.d("MainActivity", "onSensorChanged: x: ${event.values[0]}, y: ${event.values[1]}, z: ${event.values[2]}")
        }

        // 센서값이 변경될 때마다 TiltView의 onSensorEvent() 메서드 호출
        tiltView.onSensorEvent(event!!)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}