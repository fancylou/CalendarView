package net.zoneland.o2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_day.setOnClickListener {
            ScheduleViewActivity.start(this, false)
        }
        btn_week.setOnClickListener {
            ScheduleViewActivity.start(this, true)
        }
    }
}
