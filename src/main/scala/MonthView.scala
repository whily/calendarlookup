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
  private val maxItemsPerRow = 6

  private val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)

  override def onTouchEvent(event: MotionEvent): Boolean = {
    gestureDetector.onTouchEvent(event)
    true
  }

  //override protected onMeasure(widthSpec: Int, heightSpec: Int) {
  //  val height = 1
  //  setMeasuredDimension(widthSpec, height)
  //}

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (!showing)
      return

    assert((daysPerMonth == 29) || (daysPerMonth == 30))
    val sexagenaryTexts = sexagenaries(sexagenary1stDay, daysPerMonth)

    var itemsPerRow = Math.floor(canvas.getWidth() * 1.0 / gridWidth).toInt
    if (itemsPerRow > maxItemsPerRow) {
      itemsPerRow = maxItemsPerRow
    }
    val viewWidth = itemsPerRow * gridWidth + gridWidth * 0.1f

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

    // Coordinates
    // TODO: align MonthView with the buttones above.
    val left = getPaddingLeft() / 2
    val top = getPaddingTop() / 2
    val yearX = left + sp2px(10, context)
    val yearY = top + sp2px(30, context)    
    val monthX = yearX
    val monthY = yearY + gridHeight

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

    // TODO: mark the current date

    // Start coordinates for dates
    val dateStartX = yearX
    val dateStartY = monthY + gridHeight    
    // Offset for dates.
    val dateOffsetX = sp2px(sexagenaryTextSizeSp * 11 / 5, context)
    // Negative sign since we will draw the date text higher.
    val dateOffsetY = -sp2px(sexagenaryTextSizeSp * 9 / 20, context)
    // We're writing the date in vertical way. So we need another offset for the 2nd character.

    // Draw the outline of month view.
    val rows = Math.ceil(daysPerMonth * 1.0 / itemsPerRow).toInt
    val viewHeight = dateStartY + (rows - 0.5f) * gridHeight
    paint.setColor(barColor)
    paint.setStyle(Paint.Style.STROKE)
    canvas.drawRect(left, top, left + viewWidth, viewHeight, paint)

    // Set the height.
    getLayoutParams().height = viewHeight.toInt

    for (row <- 0 until rows) {
      for (col <- 0 until itemsPerRow) {
        val index = row * itemsPerRow + col
        if (index < daysPerMonth) {
          // Write sexagenary text, in horizontal way.
          var x = dateStartX + col * gridWidth
          var y = dateStartY + row * gridHeight
          paint.setTextSize(sexagenaryTextSizePx)
          paint.setColor(sexagenaryColor)
          canvas.drawText(sexagenaryTexts(index), x, y, paint)

          // Write date text, in vertical way.
          val dateText = Dates(index)
          paint.setTextSize(dateTextSizePx)
          paint.setColor(dateColor)          
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
      } else if (e2.getX() - e1.getX() < -threshold) {
      }

      if (newChineseDate != null)
        searchActivity.showMonthView(newChineseDate)

      true
    }

    override def onShowPress(event: MotionEvent) {
    }

    override def onSingleTapConfirmed(event: MotionEvent): Boolean = {
      true
    }        
  }  
}
