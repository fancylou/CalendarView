package net.zoneland.o2.view

import android.content.Context
import android.graphics.*
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import android.text.*
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import net.zoneland.o2.view.ext.Ext
import net.zoneland.o2.view.listener.OnEventClickListener
import net.zoneland.o2.view.listener.OnEventLongPressListener
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by fancyLou on 01/06/2018.
 */


class ScheduleView : View {

    private val TAG = "ScheduleView"


    companion object {
        val VIEWTYPE_WEEK = 0
        val VIEWTYPE_DAY = 1
        val DEFAULT_EVENT_COLOR = Color.rgb(174, 208, 238)
    }

    private enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }


    private val TIME_FORMAT_SMAPLE = "00:00"
    private val TIME_FORMAT = "hh:mm"
    private var viewType: Int = VIEWTYPE_WEEK
    private var mContext: Context
    private var mPosition = 0

    private val mShowDayList = ArrayList<Date>()

    //左边时间列表画笔
    private val mTimeTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    //左边时间列宽度
    private var mTimeTextWidth: Float = 0f
    private var mTimeTextHeight: Float = 0f
    //字体大小 左边时间列
    private var mTimeTextSize = 12
    private var mTimeColumnTextColor = Color.BLACK
    //字体颜色
    private var mHeaderColumnTextColor = Color.BLACK
    //字体大小  头部周、日期等字体
    private var mHeaderTextSize = 12
    private var mHeaderColumnPadding = 10
    private var mHeaderMarginBottom: Float = 0f
    private var mHeaderColumnWidth: Float = 0f
    //头部文字画笔
    private val mHeaderTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mHeaderTextHeight: Float = 0f
    private var mHeaderTextGap = 10
    //头部背景画笔
    private val mHeaderBackgroundPaint: Paint by lazy { Paint() }
    private var mHeaderRowBackgroundColor = Color.WHITE
    //day 背景画笔
    private val mDayBackgroundPaint: Paint by lazy { Paint() }
    private var mDayBackgroundColor = Color.rgb(245, 245, 245)
    //未来时间段的背景画笔
    private val mFutureBackgroundPaint: Paint by lazy { Paint() }
    private var mFutureBackgroundColor = Color.rgb(252, 252, 252)
    //已经过去的时间段背景画笔
    private val mPastBackgroundPaint: Paint by lazy { Paint() }
    private var mPastBackgroundColor = Color.rgb(244, 244, 244)
    //
//    private val mFutureWeekendBackgroundPaint: Paint by lazy { Paint() }
//    private var mFutureWeekendBackgroundColor = 0
//    //
//    private val mPastWeekendBackgroundPaint: Paint by lazy { Paint() }
//    private var mPastWeekendBackgroundColor = 0
    //每小时的分割线的画笔
    private val mHourSeparatorPaint: Paint by lazy { Paint() }
    private var mHourSeparatorHeight = 2
    private var mHourSeparatorColor = Color.rgb(230, 230, 230)
    //当前时间线的画笔
    private val mNowLinePaint: Paint by lazy { Paint() }
    private var mNowLineThickness = 5
    private var mNowLineColor = Color.rgb(51, 127, 246)
    //@date 2018-6-7 去掉今天背景色
//    private val mTodayBackgroundPaint: Paint by lazy { Paint() }
//    private var mTodayBackgroundColor = Color.rgb(239, 247, 254)
    //
    private val mTodayHeaderTextPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private var mTodayHeaderTextColor = Color.rgb(39, 137, 228)
    //事件背景画笔
    private val mEventBackgroundPaint: Paint by lazy { Paint() }
    //头部日历背景画笔
    private val mHeaderColumnBackgroundPaint: Paint by lazy { Paint() }
    private var mHeaderColumnBackgroundColor = Color.WHITE
    //
    private val mEventTextPaint: TextPaint by lazy { TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG) }
    private var mEventTextColor = Color.BLACK
    private var mEventTextSize = 12
    private var mDefaultEventColor = Color.rgb(217, 227, 242)

    private var mHourHeight = 50
    //每天的宽度
    private var mWidthPerDay: Float = 0f
    private var mColumnGap = 10
    private var mNumberOfVisibleDays = 7 //天数 一周 或者 一日
    //全天事件 高度
    private var mAllDayEventHeight = 100
    //头部高度 mHeaderTextHeight + mAllDayEventHeight
    private var mHeaderHeight: Float = 0f
    private var mHeaderRowPadding = 10

    private var mAreDimensionsInvalid = true
    private var mScrollToHour = -1.0
    private var mShowNowLine = true
    private var mFirstDayOfWeek = Calendar.SUNDAY

    //滚动点
    private val mCurrentOrigin = PointF(0f, 0f)

    private val eventList = ArrayList<ScheduleViewEventDrawBO>()
    //interface
    private var eventClickListener: OnEventClickListener? = null
    private var eventLongPressListener: OnEventLongPressListener? = null


    private var mMinimumFlingVelocity = 0
    private var mScaledTouchSlop = 0
    private var mGestureDetector: GestureDetectorCompat? = null
    private var mScroller: OverScroller? = null
    private var mCurrentFlingDirection = Direction.NONE
    private var mCurrentScrollDirection = Direction.NONE
    private var mHorizontalFlingEnabled = true
    private var mVerticalFlingEnabled = true
    private val mGestureListener: GestureDetector.SimpleOnGestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                // Reset scrolling and fling direction.
                mCurrentFlingDirection = Direction.NONE
                mCurrentScrollDirection = Direction.NONE
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                if (mCurrentScrollDirection == Direction.NONE) {
                    // Allow scrolling only in one direction.
                    if (Math.abs(distanceX) < Math.abs(distanceY)) {
                        mCurrentScrollDirection = Direction.VERTICAL
                    }
                }

                // Calculate the new origin after scroll.
                return when (mCurrentScrollDirection) {
                    Direction.VERTICAL -> {
                        mCurrentOrigin.y -= distanceY
                        ViewCompat.postInvalidateOnAnimation(this@ScheduleView)
                        true
                    }
                    else -> false
                }

            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled ||
                        mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled ||
                        mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled) {
                    return false
                }

                mScroller?.forceFinished(true)

                mCurrentFlingDirection = mCurrentScrollDirection
                when (mCurrentFlingDirection) {
                    Direction.VERTICAL -> {
                        mScroller?.fling(
                                mCurrentOrigin.x.toInt(),
                                mCurrentOrigin.y.toInt(),
                                0,
                                velocityY.toInt(),
                                Integer.MIN_VALUE, Integer.MAX_VALUE,
                                (-((mHourHeight * 24).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2 - height)).toInt(),
                                0
                        )
                        ViewCompat.postInvalidateOnAnimation(this@ScheduleView)
                        return true
                    }
                    else -> {
                        return false
                    }
                }

            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                if (e != null && eventClickListener != null) {
                    eventList.forEach { event ->
                        if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                            eventClickListener?.eventClick(event.event)
                            playSoundEffect(SoundEffectConstants.CLICK)
                            return super.onSingleTapConfirmed(e)
                        }
                    }
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onLongPress(e: MotionEvent?) {
                super.onLongPress(e)
                if (e != null && eventLongPressListener != null) {
                    eventList.forEach { event ->
                        if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                            eventLongPressListener?.eventLongPress(event.event)
                            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            return
                        }
                    }
                }
            }
        }
    }


    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        mContext = context
        mGestureDetector = GestureDetectorCompat(mContext, mGestureListener)
        mScroller = OverScroller(mContext, FastOutLinearInInterpolator())
        mMinimumFlingVelocity = ViewConfiguration.get(mContext).scaledMinimumFlingVelocity
        mScaledTouchSlop = ViewConfiguration.get(mContext).scaledTouchSlop

    }

    constructor(context: Context, viewType: Int,
                headerTextSize: Int? = null,
                headerTextColor: Int? = null,
                todayTextColor: Int? = null,
                timeTextSize: Int? = null,
                timeTextColor: Int? = null,
                eventTextSize: Int? = null,
                hourHeight: Int? = null,
                firstDayOfWeek: Int = Calendar.SUNDAY) : this(context) {
        mHeaderTextSize = headerTextSize ?: context.sp(14)
        mHeaderColumnTextColor = headerTextColor ?: Color.BLACK
        mTodayHeaderTextColor = todayTextColor ?: Color.rgb(39, 137, 228)
        mTimeTextSize = timeTextSize ?: context.sp(14)
        mTimeColumnTextColor = timeTextColor ?: Color.BLACK
        mEventTextSize = eventTextSize ?: context.sp(10)
        mFirstDayOfWeek = firstDayOfWeek
        mHourHeight = hourHeight ?: context.dip(50)

        //下面的参数暂时不开放设置
        mAllDayEventHeight = context.dip(50)
        mHeaderRowPadding = context.dip(8)
        mColumnGap = 0
        mHeaderTextGap = context.dip(5)
        mHeaderColumnPadding = context.dip(10)
        mEventTextColor = Color.WHITE


        this.viewType = viewType
        mNumberOfVisibleDays = when (viewType) {
            VIEWTYPE_DAY -> 1
            else -> 7
        }
    }

    /**
     * 初始化方法 必须执行
     */
    fun initDrawThing() {
        //init 时间列
        mTimeTextPaint.textAlign = Paint.Align.RIGHT
        mTimeTextPaint.textSize = mTimeTextSize.toFloat()
        mTimeTextPaint.color = mTimeColumnTextColor
        val rect = Rect()
        mTimeTextPaint.getTextBounds(TIME_FORMAT_SMAPLE, 0, TIME_FORMAT_SMAPLE.length, rect)
        mTimeTextHeight = rect.height().toFloat()
        mHeaderMarginBottom = mTimeTextHeight / 2
        mTimeTextWidth = mTimeTextPaint.measureText(TIME_FORMAT_SMAPLE)
        //init 头部
        // Measure settings for header row.
        mHeaderTextPaint.color = mHeaderColumnTextColor
        mHeaderTextPaint.textAlign = Paint.Align.CENTER
        mHeaderTextPaint.textSize = mHeaderTextSize.toFloat()
        mHeaderTextPaint.typeface = Typeface.DEFAULT_BOLD
        mHeaderTextPaint.getTextBounds(TIME_FORMAT_SMAPLE, 0, TIME_FORMAT_SMAPLE.length, rect)
        mHeaderTextHeight = rect.height().toFloat() * 2 + mHeaderTextGap

        // Prepare header background paint.
        mHeaderBackgroundPaint.color = mHeaderRowBackgroundColor

        // Prepare day background color paint.
        mDayBackgroundPaint.color = mDayBackgroundColor
        mFutureBackgroundPaint.color = mFutureBackgroundColor
        mPastBackgroundPaint.color = mPastBackgroundColor

//        mFutureWeekendBackgroundPaint.color = mFutureWeekendBackgroundColor
//        mPastWeekendBackgroundPaint.color = mPastWeekendBackgroundColor

        // Prepare hour separator color paint.
        mHourSeparatorPaint.style = Paint.Style.STROKE
        mHourSeparatorPaint.strokeWidth = mHourSeparatorHeight.toFloat()
        mHourSeparatorPaint.color = mHourSeparatorColor

        // Prepare the "now" line color paint
        mNowLinePaint.strokeWidth = mNowLineThickness.toFloat()
        mNowLinePaint.color = mNowLineColor

        // Prepare today background color paint.
//        mTodayBackgroundPaint.color = mTodayBackgroundColor

        // Prepare today header text color paint.
        mTodayHeaderTextPaint.textAlign = Paint.Align.CENTER
        mTodayHeaderTextPaint.textSize = mHeaderTextSize.toFloat()
        mTodayHeaderTextPaint.typeface = Typeface.DEFAULT_BOLD
        mTodayHeaderTextPaint.color = mTodayHeaderTextColor

        // Prepare event background color.
        mEventBackgroundPaint.color = DEFAULT_EVENT_COLOR

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint.color = mHeaderColumnBackgroundColor

        // Prepare event text size and color.
        mEventTextPaint.style = Paint.Style.FILL
        mEventTextPaint.color = mEventTextColor
        mEventTextPaint.textSize = mEventTextSize.toFloat()

    }


    override fun onDraw(canvas: Canvas?) {
        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        //draw time column
        drawTimeColumnAndAxes(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val flag = mGestureDetector?.onTouchEvent(event)
        if (event?.action == MotionEvent.ACTION_UP && mCurrentFlingDirection == Direction.NONE) {
            mCurrentScrollDirection = Direction.NONE
        }
        return flag ?: false
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller?.isFinished == true) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                mCurrentFlingDirection = Direction.NONE
                mCurrentScrollDirection = Direction.NONE
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && mMinimumFlingVelocity >= (mScroller?.currVelocity
                            ?: 0f)) {
                mCurrentFlingDirection = Direction.NONE
                mCurrentScrollDirection = Direction.NONE
            } else if (mScroller?.computeScrollOffset() == true) {
                mCurrentOrigin.y = mScroller?.currY?.toFloat() ?: 0f
                mCurrentOrigin.x = mScroller?.currX?.toFloat() ?: 0f
                ViewCompat.postInvalidateOnAnimation(this)
            }
        }
    }


    /********************************
     * public
     */

    /**
     * 添加日程事件
     */
    fun setEvents(result: List<CalendarViewEvent>) {
        refreshEventsAndCalEventWidth(result)
        notifyDataChanged()
    }

    fun setOnEventClickListener(listener: OnEventClickListener) {
        eventClickListener = listener
    }

    fun setOnEventLongPressListener(listener: OnEventLongPressListener) {
        eventLongPressListener = listener
    }

    /**
     * 设置当前页面的位置，position=0 就是今天
     */
    fun setScheduleViewPagerPosition(position: Int): List<Date> { //控制当前日历生成
        mPosition = position
        mShowDayList.clear()
        when (viewType) {
            VIEWTYPE_WEEK -> {
                val positionWeekDay = addDay(Date(), (mPosition) * 7)
                val day0 = getFirstDayOfWeek(positionWeekDay)
                mShowDayList.add(day0)
                mShowDayList.add(addDay(day0, 1))
                mShowDayList.add(addDay(day0, 2))
                mShowDayList.add(addDay(day0, 3))
                mShowDayList.add(addDay(day0, 4))
                mShowDayList.add(addDay(day0, 5))
                mShowDayList.add(addDay(day0, 6))
            }
            VIEWTYPE_DAY -> {
                val positionDay = addDay(Date(), mPosition)
                mShowDayList.add(positionDay)
            }
        }
        //设置当前滚动位置 默认滚动到6点，如果position等于1 滚动到当前时间
        when (mPosition) {
            0 -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                goToHour(hour.toDouble())
            }
            else -> goToHour(6.0)
        }
        invalidate()
        return mShowDayList
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour
            return
        }

        var verticalOffset = 0
        if (hour > 24)
            verticalOffset = mHourHeight * 24
        else if (hour > 0)
            verticalOffset = (mHourHeight * hour).toInt()

        if (verticalOffset > (mHourHeight * 24 - height).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom)
            verticalOffset = ((mHourHeight * 24 - height).toFloat() + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom).toInt()

        mCurrentOrigin.y = (-verticalOffset).toFloat()
        invalidate()
    }


    /********************************
     * private
     */

    private fun drawHeaderRowAndEvents(canvas: Canvas?) {
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2
        mWidthPerDay = width.toFloat() - mHeaderColumnWidth - (mColumnGap * (mNumberOfVisibleDays - 1)).toFloat()
        mWidthPerDay /= mNumberOfVisibleDays
        calculateHeaderHeight()

        if (mAreDimensionsInvalid) {
            mAreDimensionsInvalid = false
            if (mScrollToHour >= 0) {
                goToHour(mScrollToHour)
                mScrollToHour = -1.0
                mAreDimensionsInvalid = false
            }
        }

        if (mCurrentOrigin.y < height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2) {
            mCurrentOrigin.y = height.toFloat() - (mHourHeight * 24).toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() - mHeaderMarginBottom - mTimeTextHeight / 2
        }
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0f
        }

//        canvas?.clipRect(mHeaderColumnWidth,
//                mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2,
//                width.toFloat(),
//                height.toFloat(),
//                Region.Op.REPLACE)
        canvas?.save()
        canvas?.clipRect(mHeaderColumnWidth,
                mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mHeaderMarginBottom + mTimeTextHeight / 2,
                width.toFloat(),
                height.toFloat())


        var startPixel = mHeaderColumnWidth
        var lineCount = ((height.toFloat() - mHeaderHeight - (mHeaderRowPadding * 2).toFloat() -
                mHeaderMarginBottom) / mHourHeight).toInt() + 1
        lineCount *= (mNumberOfVisibleDays + 1)
        val hourLines = FloatArray(lineCount * 4)
        mShowDayList.forEach { date ->
            val isToday = isToday(date)
            val start = startPixel

            //计算时间段 是过去时间还是未来时间
            val startY = mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y
            when {
                isToday -> {
                    val now = Calendar.getInstance()
                    val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                    canvas?.drawRect(start, startY, startPixel + mWidthPerDay, startY + beforeNow, mPastBackgroundPaint)
                    canvas?.drawRect(start, startY + beforeNow, startPixel + mWidthPerDay, height.toFloat(), mFutureBackgroundPaint)
                }
                (date.before(Date())) -> {
                    canvas?.drawRect(start, startY, startPixel + mWidthPerDay, height.toFloat(), mPastBackgroundPaint)
                }
                else -> {
                    canvas?.drawRect(start, startY, startPixel + mWidthPerDay, height.toFloat(), mFutureBackgroundPaint)
                }
            }

            var i = 0
            for (hourNumber in 0..23) {
                val top = mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mCurrentOrigin.y + (mHourHeight * hourNumber).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom
                if (top > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight
                        && top < height && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start
                    hourLines[i * 4 + 1] = top
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay
                    hourLines[i * 4 + 3] = top
                    i++
                }
            }
            // Draw the lines for hours.
            canvas?.drawLines(hourLines, mHourSeparatorPaint)
            // Draw the events.
            drawEvents(date, startPixel, canvas)
            // Draw the line at the current time.
            if (mShowNowLine && isToday) {
                //val startY = mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom + mCurrentOrigin.y
                val now = Calendar.getInstance()
                val beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0f) * mHourHeight
                canvas?.drawLine(start, startY + beforeNow, startPixel + mWidthPerDay, startY + beforeNow, mNowLinePaint)
            }

            startPixel += mWidthPerDay + mColumnGap
        }
        canvas?.restore()


//        canvas?.clipRect(0f, 0f, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2, Region.Op.REPLACE)
        canvas?.save()
        canvas?.clipRect(0f, 0f, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2)
        canvas?.drawRect(0f, 0f, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint)
        canvas?.restore()

        // Draw all day label
        if (mHeaderHeight > mHeaderTextHeight) { //have all day event
            canvas?.drawText("全天", mTimeTextWidth + mHeaderRowPadding*2, mHeaderHeight, mTimeTextPaint)
        }

        // Clip to paint header row only.
        canvas?.save()
        canvas?.clipRect(mHeaderColumnWidth, 0f, width.toFloat(), mHeaderHeight + mHeaderRowPadding * 2)
        canvas?.drawRect(0f, 0f, width.toFloat(), mHeaderHeight + mHeaderRowPadding * 2, mHeaderBackgroundPaint)
        canvas?.restore()

        startPixel = mHeaderColumnWidth
        mShowDayList.forEach { date ->
            val isToday = isToday(date)
            val weekDayCh = getWeekDay(date)
            val dayString = stringDateTime("d", date)
            when (viewType) {
                VIEWTYPE_WEEK -> {
                    canvas?.drawText(weekDayCh, startPixel + mWidthPerDay / 2, mHeaderTextHeight / 2 + mHeaderRowPadding, if (isToday) mTodayHeaderTextPaint else mHeaderTextPaint)
                    canvas?.drawText(dayString, startPixel + mWidthPerDay / 2, mHeaderTextHeight + mHeaderRowPadding + mHeaderTextGap, if (isToday) mTodayHeaderTextPaint else mHeaderTextPaint)
                }
                VIEWTYPE_DAY -> {
                    val y = (mHeaderTextHeight + mHeaderRowPadding + mHeaderTextGap) - ((mHeaderTextHeight + mHeaderRowPadding + mHeaderTextGap) - (mHeaderTextHeight / 2)) / 2
                    canvas?.drawText("$weekDayCh / $dayString", startPixel + mWidthPerDay / 2, y, if (isToday) mTodayHeaderTextPaint else mHeaderTextPaint)
                }
            }

            drawAllDayEvents(date, startPixel, canvas)
            startPixel += mWidthPerDay + mColumnGap
        }

    }

    private fun drawAllDayEvents(date: Date, startPixel: Float, canvas: Canvas?) {
        val day = Calendar.getInstance()
        day.time = date
        eventList.forEachIndexed { _, eventDrawBO ->
            if ((isSameDay(eventDrawBO.event.startTime, day) || theAllDayEventCanDraw(day, eventDrawBO.event.startTime, eventDrawBO.event.endTime)) && eventDrawBO.event.isAllDay) {
                val top = mHeaderTextHeight + mHeaderRowPadding * 2 + mHeaderTextGap + eventDrawBO.top * mAllDayEventHeight
                val bottom = top + eventDrawBO.height * mAllDayEventHeight
                var right = startPixel + eventDrawBO.right * mWidthPerDay
                if (right > width) {
                    right = width.toFloat()
                }
                val rectF = RectF(startPixel, top, right, bottom)
                eventDrawBO.rectF = rectF
                val eventColor = if (eventDrawBO.event.color == 0) mDefaultEventColor else eventDrawBO.event.color
                mEventBackgroundPaint.color = eventColor
                canvas?.drawRoundRect(rectF, 0f, 0f, mEventBackgroundPaint)
                val isDark = Ext.isDarkColor(eventColor)
                drawEventTitle(eventDrawBO.event, rectF, canvas, top, startPixel, isDark)
            }
        }
    }

    private fun drawEvents(date: Date, startPixel: Float, canvas: Canvas?) {
        val day = Calendar.getInstance()
        day.time = date
        eventList.forEachIndexed { _, eventDrawBO ->
            if (isSameDay(eventDrawBO.event.startTime, day) && !eventDrawBO.event.isAllDay) {
                // Calculate top.
                val top = mHourHeight.toFloat() * 24f * eventDrawBO.top / 1440 +
                        mCurrentOrigin.y + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() +
                        mHeaderMarginBottom + mTimeTextHeight / 2
                // Calculate bottom.
                val bottom = mHourHeight.toFloat() * 24f * eventDrawBO.bottom / 1440 +
                        mCurrentOrigin.y + mHeaderHeight + (mHeaderRowPadding * 2).toFloat() +
                        mHeaderMarginBottom + mTimeTextHeight / 2
                // Calculate left and right.
                val left = startPixel + eventDrawBO.left * mWidthPerDay
                val right = left + eventDrawBO.width * mWidthPerDay
                if (left < right &&
                        left < width &&
                        top < height &&
                        right > mHeaderColumnWidth &&
                        bottom > mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                    val rectF = RectF(left, top, right, bottom)
                    eventDrawBO.rectF = rectF
                    val eventColor = if (eventDrawBO.event.color == 0) mDefaultEventColor else eventDrawBO.event.color
                    mEventBackgroundPaint.color = eventColor
                    canvas?.drawRoundRect(rectF, 0f, 0f, mEventBackgroundPaint)
                    val isDarkColor = Ext.isDarkColor(eventColor)
                    drawEventTitle(eventDrawBO.event, rectF, canvas, top, left, isDarkColor)
                }
            }
        }
    }

    /**
     * 事件的标题
     * todo 如果背景色偏淡 字体颜色设为黑色 否则白色
     */
    private fun drawEventTitle(event: CalendarViewEvent, rect: RectF, canvas: Canvas?, originalTop: Float, originalLeft: Float, isEventColorDark: Boolean) {
        val mEventPadding = 8
        if (rect.right - rect.left - (mEventPadding * 2).toFloat() < 0) return
        if (rect.bottom - rect.top - (mEventPadding * 2).toFloat() < 0) return

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        bob.append(event.name)
        bob.setSpan(StyleSpan(Typeface.BOLD), 0, bob.length, 0)
        bob.append(' ')

        val textColor = if (isEventColorDark) mEventTextColor else Color.BLACK
        mEventTextPaint.color = textColor

        val availableHeight = (rect.bottom - originalTop - (mEventPadding * 2).toFloat()).toInt()
        val availableWidth = (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt()

        // Get text dimensions.
        var textLayout = StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false)

        val lineHeight = textLayout.height / textLayout.lineCount

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            var availableLineCount = availableHeight / lineHeight
            do {
                // Ellipsize text to fit into event rect.
                textLayout = StaticLayout(TextUtils.ellipsize(bob, mEventTextPaint, (availableLineCount * availableWidth).toFloat(), TextUtils.TruncateAt.END),
                        mEventTextPaint,
                        (rect.right - originalLeft - (mEventPadding * 2).toFloat()).toInt(),
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false)

                // Reduce line count.
                availableLineCount--

                // Repeat until text is short enough.
            } while (textLayout.height > availableHeight)

            // Draw text.
            canvas?.save()
            canvas?.translate(originalLeft + mEventPadding, originalTop + mEventPadding)
            textLayout.draw(canvas)
            canvas?.restore()
        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas?) {
        // Draw the background color for the header column.
        canvas?.drawRect(0f, mHeaderHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, height.toFloat(), mHeaderColumnBackgroundPaint)
        // Clip to paint in left column only.
        canvas?.save()
        canvas?.clipRect(0f, mHeaderHeight + mHeaderRowPadding * 2, mHeaderColumnWidth, height.toFloat())
        for (i in 0..23) {
            val top = mHeaderHeight + (mHeaderRowPadding * 2).toFloat() + mCurrentOrigin.y + (mHourHeight * i).toFloat() + mHeaderMarginBottom

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            val time = if (i < 10) "0$i:00" else "$i:00"
            if (top < height) canvas?.drawText(time, mTimeTextWidth + mHeaderColumnPadding * 2, top + mTimeTextHeight, mTimeTextPaint)
        }
        canvas?.restore()
    }


    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        //全天事件
        if (eventList.size > 0) {
            for (dayNumber in mShowDayList.indices) {
                val day = Calendar.getInstance()
                day.time = mShowDayList[dayNumber]
                for (i in eventList.indices) {
                    val can = theAllDayEventCanDraw(day, eventList[i].event.startTime, eventList[i].event.endTime)
                    if ((isSameDay(eventList[i].event.startTime, day) || can) && eventList[i].event.isAllDay) {
                        containsAllDayEvent = true
                        break
                    }
                }
                if (containsAllDayEvent) {
                    break
                }
            }
        }
        mHeaderHeight = if (containsAllDayEvent) {
            mHeaderTextHeight + (mAllDayEventHeight + mHeaderMarginBottom)
        } else {
            mHeaderTextHeight
        }
    }

    private fun refreshEventsAndCalEventWidth(result: List<CalendarViewEvent>) {
        eventList.clear()
        result.sortedBy { it.startTime.timeInMillis }
        val groups = HashMap<String, ArrayList<CalendarViewEvent>>()
        val allDayKey = "allDayKey"
        result.forEach {
            if (it.isAllDay) {
                if (groups.containsKey(allDayKey)) {
                    groups[allDayKey]?.add(it)
                } else {
                    groups[allDayKey] = arrayListOf(it)
                }
            } else {
                val date = stringDateTime("yyyy-MM-dd", it.startTime.time)
                if (groups.containsKey(date)) {
                    groups[date]?.add(it)
                } else {
                    groups[date] = arrayListOf(it)
                }
            }
        }
        groups.forEach { (_, value) ->
            calEventWidthEveryday(value)
        }

    }

    private fun calEventWidthEveryday(list: ArrayList<CalendarViewEvent>) {
        //开始计算事件方块的重叠情况，计算一个最终的方块宽度
        val columns = ArrayList<ArrayList<CalendarViewEvent>>()
        columns.add(ArrayList())
        for (eventRect in list) {
            var isPlaced = false
            for (column in columns) {
                if (column.isEmpty()) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect, column[column.size - 1])) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn = ArrayList<CalendarViewEvent>()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }

        var maxRowCount = 0
        for (column in columns) {
            maxRowCount = Math.max(maxRowCount, column.size)
        }

        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val event = column[i]
                    val eventRect = ScheduleViewEventDrawBO(event)
                    if (!eventRect.event.isAllDay) {
                        eventRect.width = 1f / columns.size
                        eventRect.left = j / columns.size
                        eventRect.top = (eventRect.event.startTime.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.startTime.get(Calendar.MINUTE)).toFloat()
                        eventRect.bottom = (eventRect.event.endTime.get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.endTime.get(Calendar.MINUTE)).toFloat()
                    } else {// 全天事件 还有 跨天事件 是在头部 多个事件重叠是纵向排列
                        eventRect.height = 1f / columns.size
                        eventRect.top = j / columns.size
                        val rightIndex = eventRect.event.endTime.get(Calendar.DAY_OF_YEAR) - eventRect.event.startTime.get(Calendar.DAY_OF_YEAR)
                        //left起始  , right是跨度 跨几天
                        eventRect.left = 0f
                        eventRect.right = (rightIndex + 1).toFloat()
                    }
                    eventList.add(eventRect)
                }
                j++
            }
        }
    }


    /**
     * 事件数据变化后重绘
     */
    private fun notifyDataChanged() {
        invalidate()
    }

    private fun isEventsCollide(event: CalendarViewEvent, event1: CalendarViewEvent): Boolean {
        val start = event.startTime.timeInMillis
        val end = event.endTime.timeInMillis
        val start1 = event1.startTime.timeInMillis
        val end1 = event1.endTime.timeInMillis
        return !((start >= end1) || (end <= start1))
    }


    private fun addDay(date: Date, dayCount: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, dayCount)// 指定的时间上加上n天
        return calendar.time
    }

    /**
     * 取得指定日期所在周的第一天
     *
     */
    private fun getFirstDayOfWeek(date: Date): Date {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = mFirstDayOfWeek
        c.time = date
        c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek) // Monday
        return c.time
    }

    private fun stringDateTime(aMask: String, aDate: Date?): String {
        var returnValue = ""
        if (aDate != null) {
            val df = SimpleDateFormat(aMask, Locale.getDefault())
            returnValue = df.format(aDate)
        }
        return returnValue
    }

    /**
     * 获取星期几
     * @param date
     * @return 周日 周一 周二 周三 周四 周五 周六
     * @throws ParseException
     */
    @Throws(ParseException::class)
    private fun getWeekDay(date: Date): String {
        val c = Calendar.getInstance()
        c.time = date
        val weekDay = c.get(Calendar.DAY_OF_WEEK)
        when (weekDay) {
            Calendar.SUNDAY -> return "日"
            Calendar.MONDAY -> return "一"
            Calendar.TUESDAY -> return "二"
            Calendar.WEDNESDAY -> return "三"
            Calendar.THURSDAY -> return "四"
            Calendar.FRIDAY -> return "五"
            Calendar.SATURDAY -> return "六"
        }
        return ""

    }

    private fun isToday(date: Date): Boolean {
        val now = Calendar.getInstance()
        val dateCal = Calendar.getInstance()
        dateCal.time = date
        return now.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameDay(dayOne: Calendar, dayTwo: Calendar): Boolean {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 当前的日期是否在事件的开始日期和结束日期之间
     */
    private fun isContainDay(day: Calendar, startDay: Calendar, endDay: Calendar): Boolean {
        return when {
            isSameDay(day, startDay) -> true
            isSameDay(day, endDay) -> true
            startDay.before(day) && endDay.after(day) -> true
            else -> false
        }
    }

    /**
     * 是否是当前周期的第一天
     */
    private fun isFirstDay(dayOne: Calendar): Boolean {
        val firstDay = Calendar.getInstance()
        firstDay.time = mShowDayList[0]
        return isSameDay(firstDay, dayOne)
    }

    /**
     * 当前日期是否需要画出这个全天事件
     */
    private fun theAllDayEventCanDraw(day: Calendar, startDay: Calendar, endDay: Calendar): Boolean {
        return isFirstDay(day) && isContainDay(day, startDay, endDay)
    }
}