package net.zoneland.o2

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_schedule_view.*
import net.zoneland.o2.view.CalendarViewEvent
import net.zoneland.o2.view.listener.OnEventClickListener
import net.zoneland.o2.view.listener.OnEventLongPressListener
import net.zoneland.o2.view.listener.OnSchedulerPageChangedListener
import java.util.*

class ScheduleViewActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, isWeek: Boolean) {
            val intent = Intent(context, ScheduleViewActivity::class.java)
            intent.putExtra("week", isWeek)
            context.startActivity(intent)
        }
    }

    private val clickListener by lazy {
        object : OnEventClickListener {
            override fun eventClick(eventId: String) {
                Log.i("Activity", "clickListener event:$eventId")
            }
        }
    }
    private val longclickListener by lazy {
        object : OnEventLongPressListener {
            override fun eventLongPress(eventId: String) {
                Log.i("Activity", "eventLongPress event:$eventId")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_view)
        val isWeek = intent.extras.getBoolean("week", false)
        if (isWeek) {
            schedule_week.visibility = View.VISIBLE
            schedule_day.visibility = View.GONE
        } else {
            schedule_day.visibility = View.VISIBLE
            schedule_week.visibility = View.GONE
        }

        schedule_week.setOnEventClickListener(clickListener)
        schedule_day.setOnEventClickListener(clickListener)
        schedule_week.setOnEventLongPressListener(longclickListener)
        schedule_day.setOnEventLongPressListener(longclickListener)

        schedule_week.addEvents(obtainEvents())
        schedule_week.setOnSchedulerPageChangedListener(object : OnSchedulerPageChangedListener {
            override fun changed(showDays: List<Date>) {
                Log.i("SchedulerActivity", "showDays:${showDays.size}")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.today){
            if (schedule_week.visibility == View.VISIBLE){
                schedule_week.backToToday()
            }
            if (schedule_day.visibility == View.VISIBLE) {
                schedule_day.backToToday()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun obtainEvents(): ArrayList<CalendarViewEvent> {
        val startTime1 = Calendar.getInstance()
        startTime1.set(Calendar.HOUR_OF_DAY, 13)
        startTime1.set(Calendar.MINUTE, 0)
        val endTime1 = startTime1.clone() as Calendar
        endTime1.set(Calendar.HOUR_OF_DAY, 15)
        endTime1.set(Calendar.MINUTE, 0)
        val event1 = CalendarViewEvent("1", startTime1, endTime1, "第一个事件", Color.BLUE)

        val startTime2 = Calendar.getInstance()
        startTime2.set(Calendar.HOUR_OF_DAY, 14)
        startTime2.set(Calendar.MINUTE, 0)
        val endTime2 = startTime2.clone() as Calendar
        endTime2.set(Calendar.HOUR_OF_DAY, 15)
        endTime2.set(Calendar.MINUTE, 0)
        val event2 = CalendarViewEvent("2", startTime2, endTime2, "第2个事件", Color.RED)

        val startTime3 = Calendar.getInstance()
        startTime3.set(Calendar.HOUR_OF_DAY, 14)
        startTime3.set(Calendar.MINUTE, 0)
        val endTime3 = startTime3.clone() as Calendar
        endTime3.set(Calendar.HOUR_OF_DAY, 18)
        endTime3.set(Calendar.MINUTE, 0)
        val event3 = CalendarViewEvent("3", startTime3, endTime3, "第3个事件", Color.GREEN)

        val startTime4 = Calendar.getInstance()
        startTime4.set(Calendar.HOUR_OF_DAY, 16)
        startTime4.set(Calendar.MINUTE, 0)
        val endTime4 = startTime4.clone() as Calendar
        endTime4.set(Calendar.HOUR_OF_DAY, 17)
        endTime4.set(Calendar.MINUTE, 0)
        val event4 = CalendarViewEvent("4", startTime4, endTime4, "第4个事件", Color.YELLOW)


        val startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 13)
        startTime.set(Calendar.MINUTE, 0)
        val endTime = startTime.clone() as Calendar
        endTime.set(Calendar.HOUR_OF_DAY, 15)
        endTime.set(Calendar.MINUTE, 0)
        val event = CalendarViewEvent("5", startTime, endTime, "全天事件", Color.GRAY, true)

        val startTime21 = Calendar.getInstance()
        startTime21.set(Calendar.HOUR_OF_DAY, 13)
        startTime21.set(Calendar.MINUTE, 0)
        val endTime21 = startTime21.clone() as Calendar
        endTime21.set(Calendar.HOUR_OF_DAY, 15)
        endTime21.set(Calendar.MINUTE, 0)
        endTime21.add(Calendar.DAY_OF_YEAR, 2)
        val event21 = CalendarViewEvent("21", startTime21, endTime21, "21全天事件", Color.MAGENTA, true)

        val startTime22 = Calendar.getInstance()
        startTime22.add(Calendar.DAY_OF_YEAR, 2)
        startTime22.set(Calendar.HOUR_OF_DAY, 13)
        startTime22.set(Calendar.MINUTE, 0)
        val endTime22 = startTime22.clone() as Calendar
        endTime22.set(Calendar.HOUR_OF_DAY, 15)
        endTime22.set(Calendar.MINUTE, 0)
        val event22 = CalendarViewEvent("22", startTime22, endTime22, "22全天事件", Color.MAGENTA, true)

        return arrayListOf(event1, event2, event3, event4, event, event21, event22)
    }
}
