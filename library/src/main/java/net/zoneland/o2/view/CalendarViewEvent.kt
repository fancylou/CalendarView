package net.zoneland.o2.view

import java.util.*

/**
 * 日程上展示用的日历事件对象
 * Created by fancyLou on 05/06/2018.
 */

data class CalendarViewEvent(
        var startTime:Calendar = Calendar.getInstance(),
        var endTime: Calendar = Calendar.getInstance(),
        var name: String = "",
        var color: Int = ScheduleView.DEFAULT_EVENT_COLOR ,
        var isAllDay: Boolean = false,
        var mData: Any? = null
)