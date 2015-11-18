/**
 * Class CalendarArrayAdapter.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License:
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2015 Yujian Zhang
 */

package net.whily.android.calendarlookup

import java.text.Normalizer
import android.content.Context
import net.whily.scaland.FilterArrayAdapter
import net.whily.chinesecalendar.ChineseCalendar

/**
 * Filter the string specific for calendar input.
 */
class CalendarArrayAdapter(context: Context, textViewResourceId: Int, objects: Array[String])
  extends FilterArrayAdapter(context, textViewResourceId, objects) {

  /** Normalize string.
    *
    * 1. For string starting from numeric (1, 2, 3), convert all Chinese number into numeric.
    * 2. For string starting from Chinese, convert numeric into Chinese.
    * 3. Convert all simplified Chinese into Traditional Chinese.
    */
  override def normalize(s: String): String = {
    val t = Normalizer.normalize(s.toLowerCase, Normalizer.Form.NFD)
    ChineseCalendar.simplified2Traditional2(t)
  }
}
