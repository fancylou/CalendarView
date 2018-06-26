package net.zoneland.o2.view

import android.content.Context
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import net.zoneland.o2.view.listener.OnEventClickListener
import net.zoneland.o2.view.listener.OnEventLongPressListener
import net.zoneland.o2.view.listener.OnSchedulerPageChangedListener
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by fancyLou on 05/06/2018.
 */


class ScheduleViewLayout : LinearLayout {

    private val TODAY_HEADER_COLOR = Color.rgb(39, 137, 228)
    private val VIEW_PAGER_START_POSITION = 500

    //自定义参数

    private var mViewType = ScheduleView.VIEWTYPE_WEEK
    private var mHeaderTextSize = 0
    private var mHeaderColumnTextColor = Color.BLACK
    private var mTodayHeaderTextColor = TODAY_HEADER_COLOR
    private var mTimeTextSize = 0
    private var mTimeColumnTextColor = Color.BLACK
    private var mEventTextSize = 0
    private var mFirstDayOfWeek = Calendar.SUNDAY
    private var mHourHeight = 0

    private var eventClickListener: OnEventClickListener? = null
    private var eventLongPressListener: OnEventLongPressListener? = null
    private var pageChangedListener: OnSchedulerPageChangedListener? = null

    private lateinit var viewPager: ViewPager
    private val adapter by lazy { SViewPagerAdapter(mViewType) }

    private val mEventList: ArrayList<ScheduleViewEvent> by lazy { ArrayList<ScheduleViewEvent>() }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {

        val array = context.obtainStyledAttributes(attributes, R.styleable.ScheduleViewLayout)
        mHeaderTextSize = array.getDimensionPixelSize(R.styleable.ScheduleViewLayout_headerTextSize, context.sp(14))
        mHeaderColumnTextColor = array.getColor(R.styleable.ScheduleViewLayout_headerTextColor, Color.BLACK)
        mTodayHeaderTextColor = array.getColor(R.styleable.ScheduleViewLayout_headerTodayTextColor, TODAY_HEADER_COLOR)
        mTimeTextSize = array.getDimensionPixelSize(R.styleable.ScheduleViewLayout_timeTextSize, context.sp(14))
        mTimeColumnTextColor = array.getColor(R.styleable.ScheduleViewLayout_timeTextColor, Color.BLACK)
        mEventTextSize = array.getDimensionPixelSize(R.styleable.ScheduleViewLayout_eventTextSize, context.sp(10))
        mViewType = array.getInt(R.styleable.ScheduleViewLayout_viewType, ScheduleView.VIEWTYPE_WEEK)
        mFirstDayOfWeek = array.getInt(R.styleable.ScheduleViewLayout_firstDayOfWeek, Calendar.SUNDAY)
        mHourHeight = array.getDimensionPixelSize(R.styleable.ScheduleViewLayout_hourHeight, context.dip(50))
        array.recycle()
        init(context)
    }

    /**************
     * public
     */

    fun backToToday(){
        viewPager.setCurrentItem(VIEW_PAGER_START_POSITION, false)
    }
    fun addEvent(event: ScheduleViewEvent) {
        mEventList.add(event)
        adapter.setEventList(mEventList)
    }

    fun addEvents(events: List<ScheduleViewEvent>) {
        mEventList.addAll(events)
        adapter.setEventList(mEventList)
    }

    fun removeEvents() {
        mEventList.clear()
        adapter.setEventList(mEventList)
    }

    fun setOnEventClickListener(listener: OnEventClickListener) {
        eventClickListener = listener
    }

    fun setOnSchedulerPageChangedListener(listener: OnSchedulerPageChangedListener) {
        pageChangedListener = listener
    }

    fun setOnEventLongPressListener(listener: OnEventLongPressListener) {
        eventLongPressListener = listener
    }

    private fun init(context: Context) {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.schedule_view, this, true)
        viewPager = view.findViewById(R.id.sv_viewpager)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(VIEW_PAGER_START_POSITION, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val list = adapter.currentShowDays(position)
                if (list.isNotEmpty()){
                    pageChangedListener?.changed(list)
                }else{
                    Log.i("ScheduleViewLayout", "OnSchedulerPageChangedListener changed , showDays list is empty , maybe ScheduleViewLayout is not show")
                }
            }
        })
    }

    inner class SViewPagerAdapter(val viewType: Int) : PagerAdapter() {
        private val currentViews: ArrayDeque<ScheduleView> by lazy { ArrayDeque<ScheduleView>() }
        private val mEventList: ArrayList<ScheduleViewEvent> by lazy { ArrayList<ScheduleViewEvent>() }
        private val mShowDays: SparseArray<List<Date>> by lazy { SparseArray<List<Date>>() }
        init {
            currentViews.iterator()
        }

        fun setEventList(eventList: ArrayList<ScheduleViewEvent>) {
            mEventList.clear()
            mEventList.addAll(eventList)
            currentViews.forEach { view ->
                view.setEvents(eventList)
            }
        }

        fun currentShowDays(position: Int): List<Date> {
            return try {
                mShowDays.get(position)?: arrayListOf()
            } catch (e: Exception){
                arrayListOf()
            }
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount(): Int = 1000

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = getItemView(container, position)
            val param = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            container.addView(view, param)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            currentViews.remove(`object` as ScheduleView)
            mShowDays.remove(position)
            container.removeView(`object`)
        }

        private fun getItemView(container: ViewGroup, position: Int): View {
            val mPosition = calBizPosition(position)
            val view = ScheduleView(
                    container.context,
                    viewType,
                    headerTextColor = mHeaderColumnTextColor,
                    headerTextSize = mHeaderTextSize,
                    todayTextColor = mTodayHeaderTextColor,
                    timeTextSize = mTimeTextSize,
                    timeTextColor = mTimeColumnTextColor,
                    eventTextSize = mEventTextSize,
                    hourHeight = mHourHeight,
                    firstDayOfWeek = mFirstDayOfWeek
            )
            view.initDrawThing()//一定要执行
            view.tag = mPosition
            view.setOnEventClickListener(object : OnEventClickListener {
                override fun eventClick(eventId: String) {
                    eventClickListener?.eventClick(eventId)
                }
            })
            view.setOnEventLongPressListener(object : OnEventLongPressListener {
                override fun eventLongPress(eventId: String) {
                    eventLongPressListener?.eventLongPress(eventId)
                }
            })
            mShowDays.put(position, view.setScheduleViewPagerPosition(mPosition))
            currentViews.add(view)
            view.setEvents(mEventList)
            return view
        }

        private fun calBizPosition(position: Int): Int {
            return position - VIEW_PAGER_START_POSITION
        }
    }


}