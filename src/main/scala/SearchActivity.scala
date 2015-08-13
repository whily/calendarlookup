/**
 * Search activity for Calendar Lookup.
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
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.{Editable, TextWatcher}
import android.view.{Menu, MenuItem, MotionEvent, View}
import android.view.inputmethod.InputMethodManager
import android.util.{Log, TypedValue}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, Button, TextView}
import net.whily.scaland.{ExceptionHandler, Util}
import net.whily.chinesecalendar.ChineseCalendar
import net.whily.chinesecalendar.ChineseCalendar._
import net.whily.chinesecalendar.Chinese._

class SearchActivity extends Activity {
  private var inputView: InputView = null
  private var bar: ActionBar = null
  private var searchEntry: AutoCompleteTextView = null
  private var clearButton: Button = null
  private var jgCalendarTextView: TextView = null
  private var altCalendarButtons: Array[Button] = null
  private var monthView: MonthView = null
  private val ResultSettings = 1
  private val guideText = "......"
  private var displaySimplified = true
  // Since the app autmatically fills searchEntry if there is only one
  // alternative, backspace mode is introduced to turn off this
  // behavior, i.e. when user presses backspace, automatical filling
  // is disabled until user enteres new text again.
  private var backspaceMode = false
  private var prevText = ""
  private var defaultSearchTextColor: ColorStateList = null

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
      case android.R.id.home | R.id.settings =>
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
          queryAndShowSafe(altCalendarButton.getText().toString(), true)
        }
      })
    }

    inputView = findViewById(R.id.input).asInstanceOf[InputView]
    inputView.searchActivity = this

    monthView = findViewById(R.id.month).asInstanceOf[MonthView]
    monthView.searchActivity = this

    clearButton = findViewById(R.id.clear_button).asInstanceOf[Button]
    clearButton.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View) {
        searchEntry.setText("")
        searchEntry.setTextColor(defaultSearchTextColor)
      }
    })

    searchEntry = findViewById(R.id.search_entry).asInstanceOf[AutoCompleteTextView]
    searchEntry.setThreshold(1)
    defaultSearchTextColor = searchEntry.getTextColors()
    searchEntry.setOnTouchListener(new View.OnTouchListener() {
      override def onTouch(v: View, e: MotionEvent): Boolean = {
      	searchEntry.showDropDown()
        searchEntry.setTextColor(defaultSearchTextColor)
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

        val newText = searchEntry.getText().toString

        backspaceMode = (newText.length == prevText.length - 1) && prevText.startsWith(newText)
        prevText = newText

        checkInput()

        queryAndShowSafe(searchEntry.getText().toString, true)
      }

      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
      }
    })
  }

  def queryAndShow(s: String, historyUpdateNeeded: Boolean) {
    val queryText = simplified2Traditional(s.toString())
    var altCalendars: Array[String] = null

    if (queryText == "") {
      setSearchEntryNames()
    }

    // Unify the search for inpu from both Chinese Calendar
    // and Julian/Gregorian Calendar.

    val jgEntry = jgQuery(queryText)

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
    if (historyUpdateNeeded) {
      updateHistory(queryText)
    }
  }

  /** queryandShow with exception handling. */
  def queryAndShowSafe(s: String, historyUpdateNeeded: Boolean) {
    try {
      queryAndShow(s.toString, historyUpdateNeeded)
    } catch {
      case ex: Exception =>
        jgCalendarTextView.setText(guideText) //Util.exceptionStack(ex))
        for (i <- 0 until altCalendarButtons.length) {
          altCalendarButtons(i).setVisibility(View.GONE)
        }
        monthView.showing = false
        monthView.setVisibility(View.GONE)
        monthView.invalidate()
    }
  }

  /** Add character to the end of searchEntry. */
  def addInput(input: String) {
    val newText = searchEntry.getText().toString() + input
    searchEntry.setText(newText)
    Util.moveCursorToEnd(searchEntry)
  }

  /** Prepare InputView based on the content of searchEntry. */
  def checkInput() {
    val query = simplified2Traditional(searchEntry.getText().toString())
    val input = nextCharacter(query)

    def showCandidates() {
      inputView.setCandidates(input.map(normalizeChinese(_)))
      inputView.setVisibility(View.VISIBLE)
      // In backspace mode, keep IME open so user can press backspace again.
      if (!backspaceMode) {
        Util.hideSoftInput(this, searchEntry)
      }
    }

    input match {
      case null => // Show IME
        inputView.setVisibility(View.GONE)

      case Array("") => // Input is completed.
        inputView.setVisibility(View.GONE)
        Util.hideSoftInput(this, searchEntry)

      case Array(x) =>
        if (!backspaceMode) {
          addInput(normalizeChinese(x))
          inputView.setVisibility(View.GONE)
          checkInput()
        } else {
          showCandidates()
        }

      case _ =>
        showCandidates()
    }
  }

  private def getDisplayNames() = {
    val names = getHistory.reverse.filter(_ != "") ++ eraNames()
    val displayChinese = Util.getChinesePref(this)
    if (displayChinese != "simplified") {
      displaySimplified = false
    }
    names.map(normalizeChinese(_))
  }

  private def setSearchEntryNames() {
    searchEntry.setAdapter(new CalendarArrayAdapter(this, R.layout.simple_dropdown_item_1line,
      getDisplayNames()))
  }

  // Initialize the contents of the widgets.
  private def initContents() {
    backspaceMode = false
    prevText = ""

    setSearchEntryNames()
    searchEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        //
      }
    })

    checkInput()
  }

  private def showMonthView(chineseDate: ChineseCalendar) {
    val yearSexagenary = "歲次" + chineseDate.yearSexagenary()
    monthView.chineseDate = chineseDate
    monthView.selectedIndex = chineseDate.dayDiff()
    monthView.year = normalizeChinese(chineseDate.era + normalizeYear(chineseDate.year))
    monthView.yearSexagenary = normalizeChinese(yearSexagenary)
    monthView.month = normalizeChinese(normalizeMonth(chineseDate.month, chineseDate.era))
    monthView.sexagenary1stDay = sexagenary1stDayOfMonth(chineseDate)
    monthView.daysPerMonth = monthLength(chineseDate)
    monthView.showing = true
    monthView.setVisibility(View.VISIBLE)
    monthView.invalidate()
  }

  private def normalizeChinese(s: String) =
    if (displaySimplified && (s != "乾")) {
      traditional2Simplified(s)
    } else s

  private val historyPreference = "histrory_preference"
  private val historySize = 7

  /** Return previous search strings. History is stored as strings (in
    * Traditional Chinese) separated with spaces. */
  private def getHistory() = {
    val history = Util.getSharedPref(this, historyPreference, "")
    if (history == Array("")) Array[String]()
    else history.split(" ")
  }

  /** Update the history with the new input string `s`. Oldest item is
    * at the beginning. */
  private def updateHistory(s: String) {
    val t = simplified2Traditional(s)
    var history = collection.mutable.ArrayBuffer[String]() ++= getHistory()
    if (!history.exists(_.startsWith(t))) {
      val i = history.indexWhere(t.startsWith(_))
      if (i >= 0) {
        history(i) = t
      } else {
        history += t
        if (history.length > historySize) {
          history.remove(0)
        }
      }
    }
    Util.setSharedPref(this, historyPreference, history.mkString(" "))
  }

  def dimSearchTextColor() {
    searchEntry.setTextColor(Color.GRAY)
  }
}
