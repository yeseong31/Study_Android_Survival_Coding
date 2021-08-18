package com.example.stopwatch

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sub_layout1.view.*
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    lateinit var myHelper: MyDBHelper
    lateinit var sqlDB: SQLiteDatabase
    private var time = 0
    private var mTime = 0
    private var isRunning = false
    private var timerTask: Timer? = null
    private var mTimerTask: Timer? = null
    private var lastTime = 0
    private var lastMiniTime = 0
    private var lap = 1

    inner class Laps() {
        lateinit var interval: String
        lateinit var record: String
        lateinit var total: String
    }
    var lapList = ArrayList<Laps>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "스톱워치"

        listView1.adapter = MyAdapter(this)
        myHelper = MyDBHelper(this)

        sqlDB = myHelper.writableDatabase
        myHelper.onUpgrade(sqlDB, 1, 2)
        sqlDB.close()

        fab.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) {
                start()
                lapButton.setImageResource(R.drawable.ic_baseline_access_time_24)
            } else {
                pause()
                lapButton.setImageResource(R.drawable.ic_baseline_refresh_24)
            }
        }
        lapButton.setOnClickListener {
            if (isRunning) {
                if (lapList.size == 0) {
                    sqlDB = myHelper.writableDatabase
                    myHelper.onUpgrade(sqlDB, 1, 2)
                    sqlDB.close()
                }
                recordLapTime()
                mini_min.visibility = View.VISIBLE
                mini_dot1.visibility = View.VISIBLE
                mini_sec.visibility = View.VISIBLE
                mini_dot2.visibility = View.VISIBLE
                mini_milli.visibility = View.VISIBLE
            } else if (time > 0) {
                reset()
                mini_min.visibility = View.INVISIBLE
                mini_dot1.visibility = View.INVISIBLE
                mini_sec.visibility = View.INVISIBLE
                mini_dot2.visibility = View.INVISIBLE
                mini_milli.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu1, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.prev_record -> {
                if (!isRunning) {
                    sqlDB = myHelper.readableDatabase
                    val cursor = sqlDB.rawQuery("select * from groupTBL;", null)

                    loadData()

                    lapList.clear()
                    while (cursor.moveToNext()) {
                        val l = Laps()
                        l.interval = cursor.getString(0)
                        l.record = cursor.getString(1)
                        l.total = cursor.getString(2)
                        lapList.add(0, l)
                    }
                    cursor.moveToLast()
                    if (mTime > 0 && lapList.size > 0) {
                        setTime()
                        setMTime()

                        lap = cursor.getString(0).toInt() + 1
                        mini_min.visibility = View.VISIBLE
                        mini_dot1.visibility = View.VISIBLE
                        mini_sec.visibility = View.VISIBLE
                        mini_dot2.visibility = View.VISIBLE
                        mini_milli.visibility = View.VISIBLE
                    }

                    (listView1.adapter as MyAdapter).notifyDataSetChanged()
                    cursor.close()
                    sqlDB.close()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun start() {
        fab.setImageResource(R.drawable.ic_baseline_pause_24)

        if (lapList.size > 0) {
            mTimerTask = timer(period = 10) {
                mTime++
                runOnUiThread { setMTime() }
            }
        }
        timerTask = timer(period = 10) {
            time++
            runOnUiThread { setTime() }
        }
    }

    private fun pause() {
        fab.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        timerTask?.cancel()     // 실행 중인 타이머가 있다면 타이머를 취소
        mTimerTask?.cancel()
    }

    private fun recordLapTime() {
        val lapTime = this.time
        val min = if (lapTime / 6000 < 1) "0${lapTime / 6000}" else "${lapTime / 6000}"
        val sec = if (lapTime / 100 < 10) "0${lapTime / 100}" else "${lapTime / 100}"
        val milli = if (lapTime % 100 < 10) "0${lapTime % 100}" else "${lapTime % 100}"

        val mLapTime = if (mTime == 0) time else mTime
        val m = if (mLapTime / 6000 < 1) "0${mLapTime / 6000}" else "${mLapTime / 6000}"
        val s = if (mLapTime / 100 < 10) "0${mLapTime / 100}" else "${mLapTime / 100}"
        val mil = if (mLapTime % 100 < 10) "0${mLapTime % 100}" else "${mLapTime % 100}"

        val laps = Laps()
        laps.interval = if (lap < 10) "0$lap" else "$lap"
        laps.record = "${m}:${s}.${mil}"
        laps.total = "${min}:${sec}.${milli}"
        lapList.add(0, laps)

        mTimerTask?.cancel()
        mTime = 0
        mini_min.text = "00"
        mini_sec.text = "00"
        mini_milli.text = "00"

        mTimerTask = timer(period = 10) {
            mTime++
            runOnUiThread { setMTime() }
        }

        // 랩 타임 추가
        (listView1.adapter as MyAdapter).notifyDataSetChanged()
        lap++

        sqlDB = myHelper.writableDatabase
        sqlDB.execSQL("insert into groupTBL values('" + laps.interval + "', '" + laps.record + "', '" + laps.total + "');")
        sqlDB.close()
    }

    private fun reset() {
        timerTask?.cancel()

        saveData(if (time != 0) time else lastTime, if (time != 0) mTime else lastMiniTime)

        // 모든 변수 초기화
        time = 0
        mTime = 0
        isRunning = false
        fab.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        minTextView.text = "00"
        secTextView.text = "00"
        milliTextView.text = "00"
        mini_min.text = "00"
        mini_sec.text = "00"
        mini_milli.text = "00"

        // 모든 랩타임 제거
        lapList.clear()
        (listView1.adapter as MyAdapter).notifyDataSetChanged()
        lap = 1
    }

    private fun setTime() {
        val min = time / 6000
        val sec = time / 100
        val milli = time % 100
        minTextView.text = if (min < 1) "0$min" else "$min"
        secTextView.text = if (sec < 10) "0$sec" else "$sec"
        milliTextView.text = if (milli < 10) "0$milli" else "$milli"
    }

    private fun setMTime() {
        val min = mTime / 6000
        val sec = mTime / 100
        val milli = mTime % 100
        mini_min.text = if (min < 1) "0$min" else "$min"
        mini_sec.text = if (sec < 10) "0$sec" else "$sec"
        mini_milli.text = if (milli < 10) "0$milli" else "$milli"
    }

    private fun saveData(last: Int, mLast: Int) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = pref.edit()
        editor.putInt("KEY_LAST", last)
            .putInt("KEY_M_LAST", mLast)
            .apply()
    }

    private fun loadData() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val t = pref.getInt("KEY_LAST", 0)
        val m = pref.getInt("KEY_M_LAST", 0)

        if (t != 0 && m != 0) {
            if (lapList.size <= 0) {
                time = t
                mTime = m
            }
        }
    }

    inner class MyAdapter(private val context: Context): BaseAdapter() {
        override fun getCount(): Int {
            return lapList.size + 1
        }

        override fun getItem(p0: Int): Any {
            return 0
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            var itemView = p1
            if (itemView == null)
                itemView = View.inflate(context, R.layout.sub_layout1, null)
            if (p0 <= 0) {
                itemView!!.interval.text = "구간"
                itemView.record.text = "구간기록"
                itemView.total.text = "전체 시간"
                itemView.interval.setTextColor(Color.CYAN)
                itemView.record.setTextColor(Color.CYAN)
                itemView.total.setTextColor(Color.CYAN)
            } else {
                itemView!!.interval.text = lapList[p0 - 1].interval
                itemView.record.text = lapList[p0 - 1].record
                itemView.total.text = lapList[p0 - 1].total
            }
            return itemView
        }
    }

    inner class MyDBHelper(context: Context) : SQLiteOpenHelper(context, "groupDB", null, 1) {
        override fun onCreate(p0: SQLiteDatabase?) {
            p0!!.execSQL("CREATE TABLE groupTBL ( gInterval CHAR(20) PRIMARY KEY, gRecord CHAR(20), gTotal CHAR(20));")
        }
        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            p0!!.execSQL("DROP TABLE IF EXISTS groupTBL")
            onCreate(p0)
        }
    }
}