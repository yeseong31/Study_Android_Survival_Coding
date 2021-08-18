package com.example.xylophone

import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        SoundPool.Builder().setMaxStreams(8).build()
    } else {
        SoundPool(8, AudioManager.STREAM_MUSIC, 0)
    }

    private val sounds = listOf(
            Pair(R.id.do1, R.raw.do1),
            Pair(R.id.re1, R.raw.re),
            Pair(R.id.mi1, R.raw.mi),
            Pair(R.id.fa1, R.raw.fa),
            Pair(R.id.sol1, R.raw.sol),
            Pair(R.id.la1, R.raw.la),
            Pair(R.id.si1, R.raw.si),
            Pair(R.id.do2, R.raw.do2)
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면이 가로 모드로 고정되게 하기
        // requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sounds.forEach { tune(it) }
    }

    private fun tune(pitch: Pair<Int, Int>) {
        val soundId = soundPool.load(this, pitch.second, 1)
        findViewById<TextView>(pitch.first).setOnClickListener {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}