package net.zoneland.o2.view.listener

import java.util.*

/**
 * Created by fancyLou on 21/06/2018.
 */

interface OnSchedulerPageChangedListener {
    fun changed(showDays: List<Date>)
}