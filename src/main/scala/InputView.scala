/**
 * Class InputView.
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

/**
  * Show a list of input candidates (with only 1 character). Similiar to an IME.
  */
class InputView(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  // Parameters that can be changed in the runtime.
  var searchActivity: SearchActivity = null
  private var candidates: Array[String] = null
  private var selectedIndex = -1

  // Detect gestures of touch and scroll.
  private val gestureDetector = new GestureDetector(context, new MyGestureListener())

  private val inputTextSizeSp = 18
  private val inputTextSizePx = sp2px(inputTextSizeSp, context).toInt

  private val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)
  paint.setTextSize(inputTextSizePx)

  private val gridWidth = paint.measureText("一二")
  private val gridHeight = inputTextSizePx * 2

  private var left = 0.0f
  private var top = 0.0f
  private var right = 0.0f
  private var bottom = 0.0f

  private var itemsPerRow = 0
  private var rows = 0  

  override def onTouchEvent(event: MotionEvent): Boolean = {
    gestureDetector.onTouchEvent(event)
    true
  }

  def setCandidates(cs: Array[String]) {
    if ((cs.length < 1) || (cs.exists(_.length != 1))) {
      throw new IllegalArgumentException("setCandidates(): invalid candidates " +
        cs.mkString(" "))
    }
    candidates = cs
    selectedIndex = -1
  }

  // Calculate measures to determin the width/height of the view.
  private def calculateMeasure(width: Int) { 
    itemsPerRow = Math.floor(width * 1.0 / gridWidth).toInt
    right = width
    if (candidates != null) {
      rows = Math.ceil(candidates.length * 1.0 / itemsPerRow).toInt
      bottom = rows * gridHeight
    }
  }

  override protected def onMeasure(widthSpec: Int, heightSpec: Int) {
    val width = MeasureSpec.getSize(widthSpec)
    calculateMeasure(width)
    setMeasuredDimension(widthSpec, if (showing) bottom.toInt else 0)
  }

  /** Whether the view should be showed. */
  def showing = candidates != null

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    if (!showing)
      return

    calculateMeasure(canvas.getWidth())
    // Force to use the correct layout.
    requestLayout()

    val backgroundNormalColor = Color.rgb(228, 228, 228)
    val backgroundSelectedColor = Color.rgb(190, 190, 190)
    val textNormalColor = Color.rgb(140, 140, 140)
    val textSelectedColor = Color.rgb(110, 110, 110)    
    val lineColor = textNormalColor

    paint.setColor(backgroundNormalColor)
    paint.setStyle(Paint.Style.FILL)
    canvas.drawRect(left, top, right, bottom, paint)

    for (row <- 0 until rows) {
      for (col <- 0 until itemsPerRow) {
        val index = row * itemsPerRow + col
        if (index < candidates.length) {
          val text = candidates(index)
          val textWidth = paint.measureText(text)
          val textX = col * gridWidth + (gridWidth - textWidth) / 2
          val textY = (row + 0.68f) * gridHeight
          val lineX = (col + 1) * gridWidth
          val lineY = row * gridHeight

          paint.setColor(lineColor)
          if (col < itemsPerRow - 1) {
            canvas.drawLine(lineX, lineY + 0.25f * gridHeight,
              lineX, lineY + 0.75f * gridHeight, paint)
          }

          if (index == selectedIndex) {
            paint.setColor(backgroundSelectedColor)
            canvas.drawRect((col + 0.05f) * gridWidth, lineY,
              (col + 0.95f) * gridWidth, lineY + gridHeight, paint)
            paint.setColor(textSelectedColor)
          } else {
            paint.setColor(textNormalColor)
          }
          canvas.drawText(candidates(index), textX, textY, paint)
        }
      }
    }
  }

  private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private def pressIndex(event: MotionEvent): Option[Int] = {
      val x = event.getX()
      val y = event.getY()

      if ((left <= x) && (x < right) && (top < y) && (y < bottom)) {
        val rowIndex = Math.floor((y - top) / gridHeight).toInt
        val colIndex = Math.floor((x - left) / gridWidth).toInt
        val index = rowIndex * itemsPerRow + colIndex
        if (index < candidates.length) {
          return Some(index)
        }
      }

      None
    }

    private def tapInput(event: MotionEvent) {
      pressIndex(event) match {
        case Some(index) =>
          val selectedInput = candidates(index)
          searchActivity.addInput(selectedInput)          
        case None =>
      }    
    }

    override def onSingleTapConfirmed(event: MotionEvent): Boolean = {
      tapInput(event)
      true
    }

    override def onLongPress(event: MotionEvent) = {
      tapInput(event)
    }

    override def onShowPress(event: MotionEvent) {
      pressIndex(event) match {
        case Some(index) =>
          selectedIndex = index
          invalidate()
        case None =>
      }      
    }

    override def onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = {
      val threshold = sp2px(8, context)
      val deltaX = Math.abs(e2.getX() - e1.getX())
      val deltaY = Math.abs(e2.getY() - e1.getY())
      if ((deltaX > threshold) || (deltaY > threshold)) {
        selectedIndex = -1
      }

      true
    }
  }  
}
