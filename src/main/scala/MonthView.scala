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
import android.view.View

class MonthView(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  val paint = new Paint()
  paint.setAntiAlias(true)
  paint.setStyle(Paint.Style.STROKE)
  // paint.setColor(Color.RED)

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawText("test", 1, 20, paint)
  }
}
