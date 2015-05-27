/**
 * Class ExpandableListAdapter.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License:
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2013 Yujian Zhang
 */

package net.whily.android.calendarlookup

import android.app.Activity
import android.content.Context
import android.graphics.{Canvas, Color, Paint}
import android.util.AttributeSet
import android.view.{GestureDetector, MotionEvent, View}
import android.view.View.MeasureSpec
import net.whily.scaland.Util._
import net.whily.chinesecalendar.ChineseCalendar
import net.whily.chinesecalendar.ChineseCalendar._

class MonthView(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  // Parameters that can be changed in the runtime.
  var searchActivity: SearchActivity = null
  var chineseDate: ChineseCalendar = null
  var showing = false       // Whether to show the month view.
  var year = ""             // Year information
  var month = ""            // Month information
  var sexagenary1stDay = "" // The sexagenary of the 1st day of the month.
  var daysPerMonth = 30     // Number of days per month. Can only be 29 or 30

  // Detect gestures of touch and scroll.
  private val gestureDetector = new GestureDetector(context, new MyGestureListener())

  private val sexagenaryTextSizeSp = 18
  private val dateTextSizeSp = (sexagenaryTextSizeSp * 0.6).toInt
  private val sexagenaryTextSizePx = sp2px(sexagenaryTextSizeSp, context)  
  private val dateTextSizePx = sp2px(dateTextSizeSp, context)
  // Assuming one data occuipies one grid.
  private val gridWidth = sp2px(sexagenaryTextSizeSp * 7 / 2, context)
  private val gridHeight = sp2px(sexagenaryTextSizeSp * 5 / 3, context)
  private var calendarLeft = 0.0
  private var calendarTop = 0.0
  private var calendarRight = 0.0
  private var calendarBottom = 0.0  
  private val maxItemsPerRow = 6
  private var itemsPerRow = maxItemsPerRow
  private var viewWidth = 0.0f
  private var viewHeight = 0.0f
  private var left = 0
  private var top = 0
  private var yearX = 0.0f
  private var yearY = 0.0f
  private var monthX = 0.0f
  private var monthY = 0.0f
  private var rows = 0
  private var dateStartY = 0.0f

  private val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)

  override def onTouchEvent(event: MotionEvent): Boolean = {
    gestureDetector.onTouchEvent(event)
    true
  }

  // Calculate measures to determin the width/height of the view.
  private def calculateMeasure(width: Int) { 
    itemsPerRow = Math.floor(width * 1.0 / gridWidth).toInt
    if (itemsPerRow > maxItemsPerRow) {
      itemsPerRow = maxItemsPerRow
    }

    // Coordinates
    // TODO: align MonthView with the buttones above.
    left = getPaddingLeft() / 2
    top = getPaddingTop() / 2
    yearX = left + sp2px(10, context)
    yearY = top + sp2px(30, context)    
    monthX = yearX
    monthY = yearY + gridHeight

    dateStartY = monthY + gridHeight    

    viewWidth = itemsPerRow * gridWidth + gridWidth * 0.1f

    rows = Math.ceil(daysPerMonth * 1.0 / itemsPerRow).toInt
    viewHeight = dateStartY + (rows - 0.5f) * gridHeight    
  }

  override protected def onMeasure(widthSpec: Int, heightSpec: Int) {
    val width = MeasureSpec.getSize(widthSpec)
    calculateMeasure(width)
    setMeasuredDimension(widthSpec, if (showing) viewHeight.toInt else 0)
  }

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (!showing)
      return

    assert((daysPerMonth == 29) || (daysPerMonth == 30))
    val sexagenaryTexts = sexagenaries(sexagenary1stDay, daysPerMonth)

    calculateMeasure(canvas.getWidth())
    // Force to use the correct layout.
    requestLayout()

    // Font color of light theme.
    var sexagenaryColor = Color.BLACK 
    var dateColor = Color.DKGRAY 
    val cyan = Color.rgb(0, 150, 136)
    var barColor = cyan 
    var selectedDateColor = cyan 
    var selectedBackgroundColor = Color.rgb(182, 217, 214) 
    var yearMonthColor = Color.WHITE 
    if (getThemePref(context) == 0) { // Dark theme
      sexagenaryColor = Color.WHITE
      dateColor = Color.LTGRAY
    }

    // Draw bar for year and month text.
    paint.setColor(barColor)
    paint.setStyle(Paint.Style.FILL)
    canvas.drawRect(left, top, left + viewWidth, monthY + gridHeight * 0.15f, paint)

    // Show year.
    paint.setTextSize(sexagenaryTextSizePx)
    paint.setColor(yearMonthColor)
    canvas.drawText(year, yearX, yearY, paint)

    // Show month.

    paint.setTextSize(sexagenaryTextSizePx)
    paint.setColor(yearMonthColor)
    canvas.drawText(month, monthX, monthY, paint)

    // Start coordinates for dates
    val dateStartX = yearX
    // Offset for dates.
    val dateOffsetX = sp2px(sexagenaryTextSizeSp * 11 / 5, context)
    // Negative sign since we will draw the date text higher.
    val dateOffsetY = -sp2px(sexagenaryTextSizeSp * 9 / 20, context)
    // We're writing the date in vertical way. So we need another offset for the 2nd character.

    // Draw the outline of month view.
    paint.setColor(barColor)
    paint.setStyle(Paint.Style.STROKE)
    canvas.drawRect(left, top, left + viewWidth, viewHeight, paint)

    val leftOffset = - gridWidth / 9
    val topOffset = - gridHeight * 7 / 10
    calendarLeft = dateStartX + leftOffset
    calendarTop = dateStartY + topOffset
    calendarRight = calendarLeft + itemsPerRow * gridWidth
    calendarBottom = calendarTop + rows * gridHeight

    for (row <- 0 until rows) {
      for (col <- 0 until itemsPerRow) {
        val index = row * itemsPerRow + col
        if (index < daysPerMonth) {
          var x = dateStartX + col * gridWidth
          var y = dateStartY + row * gridHeight

          // Handle the currently selected date.
          if (index == chineseDate.dayDiff()) {
            paint.setColor(selectedBackgroundColor)
            paint.setStyle(Paint.Style.FILL)
            canvas.drawOval(x + leftOffset, y + topOffset,
              x + gridWidth + leftOffset, y + gridHeight + topOffset, paint)
          }
          val sColor = if (index == chineseDate.dayDiff()) selectedDateColor else sexagenaryColor
          val dColor = if (index == chineseDate.dayDiff()) selectedDateColor else dateColor          

          // Write sexagenary text, in horizontal way.
          paint.setTextSize(sexagenaryTextSizePx)
          paint.setColor(sColor)
          canvas.drawText(sexagenaryTexts(index), x, y, paint)

          // Write date text, in vertical way.
          val dateText = Dates(index)
          paint.setTextSize(dateTextSizePx)
          paint.setColor(dColor)          
          x += dateOffsetX
          y += dateOffsetY
          canvas.drawText(dateText.substring(0, 1), x, y, paint)
          y += dateTextSizePx
          canvas.drawText(dateText.substring(1, 2), x, y, paint)
        }
      }
    }
  }

  private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {
      val threshold = sp2px(30, context)
      var newChineseDate: ChineseCalendar = null
      if (e2.getY() - e1.getY() > threshold) {
        newChineseDate = chineseDate.lastDayPrevMonth(false)
      } else if (e2.getY() - e1.getY() < -threshold) {
        newChineseDate = chineseDate.firstDayNextMonth(false)        
      } else if (e2.getX() - e1.getX() > threshold) {
        // Last year. TODO: fix the hack.
        newChineseDate = chineseDate
        for (i <- 0 until 12) {
          newChineseDate = newChineseDate.lastDayPrevMonth(false)
        }
      } else if (e2.getX() - e1.getX() < -threshold) {
        // Next year. TODO: fix the hack.
        newChineseDate = chineseDate
        for (i <- 0 until 12) {
          newChineseDate = newChineseDate.firstDayNextMonth(false)
        }        
      }

      if (newChineseDate != null)
        searchActivity.queryAndShow(newChineseDate.toString())

      true
    }

    override def onSingleTapConfirmed(event: MotionEvent): Boolean = {
      val x = event.getX()
      val y = event.getY()

      if ((calendarLeft <= x) && (x < calendarRight) && (calendarTop < y) && (y < calendarBottom)) {
        val rowIndex = Math.floor((y - calendarTop) / gridHeight).toInt
        val colIndex = Math.floor((x - calendarLeft) / gridWidth).toInt
        val index = rowIndex * itemsPerRow + colIndex
        val newChineseDate = chineseDate.plusDays(index - chineseDate.dayDiff())
        searchActivity.queryAndShow(newChineseDate.toString())
      }

      true
    }        
  }  
}
