package com.example.bmicalculator2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 이전에 입력한 값을 읽어오기
        loadData()

        resultButton.setOnClickListener {
            // 마지막에 입력한 내용을 저장
            saveData(heightEditText.text.toString().toInt(),
                    weightEditText.text.toString().toInt())

            startActivity<ResultActivity>(
                "weight" to weightEditText.text.toString().toInt(),
                "height" to heightEditText.text.toString().toInt()
            )
        }
    }

    // 데이터 저장하기
    private fun saveData(height: Int, weight: Int) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()    // 이 객체를 사용하여 프리퍼런스에 데이터를 담을 수 있음

        editor.putInt("KEY_HEIGHT", height)
            .putInt("KEY_WEIGHT", weight)
            .apply()
    }

    // 데이터 불러오기
    private fun loadData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val height = pref.getInt("KEY_HEIGHT", 0)
        val weight = pref.getInt("KEY_WEIGHT", 0)

        if (height != 0 || weight != 0) {
            heightEditText.setText(height.toString())
            weightEditText.setText(weight.toString())
        }
    }
}