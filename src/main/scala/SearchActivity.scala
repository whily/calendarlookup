/**
 * Search activity for World Metro.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2015 Yujian Zhang
 */

package net.whily.android.calendarlookup

import scala.collection.mutable
import android.app.{ActionBar, Activity}
import android.content.Intent
import android.os.Bundle
import android.text.{Editable, TextWatcher}
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.view.inputmethod.InputMethodManager
import android.view.ViewGroup.MarginLayoutParams
import android.util.{Log, TypedValue}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, Button, TextView}
import net.whily.scaland.{ExceptionHandler, Util}
import net.whily.chinesecalendar.ChineseCalendar
import net.whily.chinesecalendar.ChineseCalendar._
import net.whily.chinesecalendar.Chinese._

class SearchActivity extends Activity {
  private var bar: ActionBar = null
  private var searchEntry: AutoCompleteTextView = null
  private var clearButton: Button = null
  private var jgCalendarTextView: TextView = null
  private var altCalendarButtons: Array[Button] = null
  private var monthView: MonthView = null
  private val ResultSettings = 1
  private val exampleText =
    Array("晉穆帝永和九年", "晉穆帝永和九年三月", "晉穆帝永和九年三月初三", "晉穆帝永和九年三月丙辰", "353年4月22日")
  private val guideText = "..." // exampleText.mkString("\n")
  private var displaySimplified = true
  
  override def onCreate(icicle: Bundle) { 
    super.onCreate(icicle)

    // Set handler for uncaught exception raised from current activity.
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this))

    Misc.setMaterialTheme(this)
    setContentView(R.layout.search)
    
    bar = getActionBar
    bar.setHomeButtonEnabled(true)
     
    initWidgets()
    initContents() 
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater().inflate(R.menu.search, menu)
    
    return super.onCreateOptionsMenu(menu)
  }    

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case android.R.id.home | R.id.about => 
        startActivity(new Intent(this, classOf[AboutActivity]))
        true
        
      case R.id.settings =>  
        startActivityForResult(new Intent(this, classOf[SettingsActivity]), ResultSettings)
        true
    }
  }
  
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    requestCode match {
      case ResultSettings => recreate() // Trigger to apply new theme.
    }
  } 
  
  // Initialize the widgets. The contents are initialized in `initContent`.
  private def initWidgets() {
    jgCalendarTextView = findViewById(R.id.jg_calendar_textview).asInstanceOf[TextView]
    jgCalendarTextView.setText(guideText)

    val altCalendarButton1 = findViewById(R.id.alt_calendar_button_1).asInstanceOf[Button]
    val altCalendarButton2 = findViewById(R.id.alt_calendar_button_2).asInstanceOf[Button]
    altCalendarButtons = Array(altCalendarButton1, altCalendarButton2)
    for (altCalendarButton <- altCalendarButtons) {
      altCalendarButton.setOnClickListener(new View.OnClickListener() {
        override def onClick(v: View) {
          queryAndShow(altCalendarButton.getText().toString())
        }
      })
    }
    val lp = altCalendarButton1.getLayoutParams().asInstanceOf[MarginLayoutParams]

    monthView = findViewById(R.id.month).asInstanceOf[MonthView]
    monthView.searchActivity = this
    monthView.leftMargin = lp.leftMargin

    clearButton = findViewById(R.id.clear_button).asInstanceOf[Button]
    clearButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        searchEntry.setText("")
      }
    })

    searchEntry = findViewById(R.id.search_entry).asInstanceOf[AutoCompleteTextView]
    searchEntry.setThreshold(1)
    searchEntry.setOnTouchListener(new View.OnTouchListener() {
      override def onTouch(v: View, e: MotionEvent): Boolean = {
      	searchEntry.showDropDown()
      	false
      }
    })
    searchEntry.addTextChangedListener(new TextWatcher() {
      override def afterTextChanged(s: Editable) {
        if (s.toString() == "") {
          clearButton.setVisibility(View.INVISIBLE)
        } else {
          clearButton.setVisibility(View.VISIBLE)
        }

        try {
          queryAndShow(s.toString)
        } catch {
          case ex: Exception =>
            jgCalendarTextView.setText("" + ex)
            for (i <- 0 until altCalendarButtons.length) {
              altCalendarButtons(i).setVisibility(View.GONE)
            }
            monthView.showing = false
            monthView.setVisibility(View.GONE)
            monthView.invalidate()
        }
      }

      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
      }
    })
  }

  def queryAndShow(s: String) {
    val queryText = simplified2Traditional(s.toString())
    var altCalendars: Array[String] = null

    // Unify the search for inpu from both Chinese Calendar
    // and Julian/Gregorian Calendar.

    // True if query is in form of Julian/Gregorian Calendar.
    val jgEntry = Character.isDigit(queryText.charAt(0)) || queryText.startsWith("公元前")

    val actualQueryText = if (jgEntry) queryText else toDate(queryText).toString()

    val result = fromDate(actualQueryText)
    val chineseDateText =
      if (jgEntry) result(0)
      else {
        val year = parseDate(queryText).era
        val Some(originalDate) = result.find(_.startsWith(year))
        originalDate
      }
    val resultFilter = result.filter(_ != chineseDateText)
    altCalendars = resultFilter.map(s => normalizeChinese(parseDate(s).toString())).toArray
    jgCalendarTextView.setText(actualQueryText)

    val altCalendarLength = if (altCalendars == null) 0 else altCalendars.length
    for (i <- 0 until altCalendarButtons.length) {
      if (i < altCalendarLength) {
        altCalendarButtons(i).setVisibility(View.VISIBLE)
        altCalendarButtons(i).setText(altCalendars(i))
      } else {
        altCalendarButtons(i).setVisibility(View.GONE)
      }
    }
    if (!((searchEntry.getSelectionStart() < queryText.length) ||
      queryText.endsWith("年") || queryText.endsWith("月"))) {
      Util.hideSoftInput(this, searchEntry)
    }
    showMonthView(parseDate(chineseDateText))
  }
  
  // Initialize the contents of the widgets.
  private def initContents() {
    val names = eraNames().map(s => s + "元年")
    val displayChinese = Util.getChinesePref(this)
    if (displayChinese != "simplified") {
      displaySimplified = false
    }
    val displayNames = names.map(normalizeChinese(_))
    searchEntry.setAdapter(new CalendarArrayAdapter(this, R.layout.simple_dropdown_item_1line,
      displayNames))
    searchEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        //
      }    
    })  
  }

  private def showMonthView(chineseDate: ChineseCalendar) {
    val yearSexagenary = "歲次" + chineseDate.yearSexagenary()
    monthView.chineseDate = chineseDate
    monthView.year = normalizeChinese(chineseDate.era + chineseDate.normalizedYear() + yearSexagenary)
    monthView.month = normalizeChinese(chineseDate.normalizedMonth())
    monthView.sexagenary1stDay = sexagenary1stDayOfMonth(chineseDate)
    monthView.daysPerMonth = monthLength(chineseDate)
    monthView.showing = true
    monthView.setVisibility(View.VISIBLE)
    monthView.invalidate()
  }

  private def normalizeChinese(s: String) =
    if (displaySimplified) traditional2Simplified(s)
    else s
}
