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

  // Detect gestures of touch and scroll.
  private val gestureDetector = new GestureDetector(context, new MyGestureListener())

  private val inputTextSizeSp = 18
  private val inputTextSizePx = sp2px(inputTextSizeSp, context).toInt

  private val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)
  paint.setTextSize(inputTextSizePx)

  private val gridWidth = paint.measureText("一二三")
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
    if ((cs.length < 2) || (cs.exists(_.length != 1))) {
      throw new IllegalArgumentException("setCandidates(): invalid candidates " +
        cs.mkString(" "))
    }
    candidates = cs
  }

  def clearCandidates() {
    candidates = null
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

    paint.setColor(Color.LTGRAY)
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

          paint.setColor(Color.DKGRAY)
          if (col < itemsPerRow - 1) {
            canvas.drawLine(lineX, lineY + 0.25f * gridHeight,
              lineX, lineY + 0.75f * gridHeight, paint)
          }
          canvas.drawText(candidates(index), textX, textY, paint)
        }
      }
    }
  }

  private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onSingleTapConfirmed(event: MotionEvent): Boolean = {
      val x = event.getX()
      val y = event.getY()

      if ((left <= x) && (x < right) && (top < y) && (y < bottom)) {
        val rowIndex = Math.floor((y - top) / gridHeight).toInt
        val colIndex = Math.floor((x - left) / gridWidth).toInt
        val index = rowIndex * itemsPerRow + colIndex
        if (index < candidates.length) {
          val selectedInput = candidates(index)
          searchActivity.addInput(selectedInput)
        }
      }

      true
    }        
  }  
}
