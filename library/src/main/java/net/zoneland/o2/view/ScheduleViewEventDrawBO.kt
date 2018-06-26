package net.zoneland.o2.view

import android.graphics.RectF

/**
 * Created by fancyLou on 05/06/2018.
 */


data class ScheduleViewEventDrawBO(
        val event: ScheduleViewEvent,
        var left: Float = 0f,
        var right: Float = 0f,
        var width: Float = 0f,//普通事件 算宽度
        var height: Float = 0f, //全天事件 算高度
        var top: Float = 0f,
        var bottom: Float = 0f,
        var rectF: RectF? = null // 计算点击事件用的
)