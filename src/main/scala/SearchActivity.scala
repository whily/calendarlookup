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
  private var resultText: TextView = null
  private var monthView: MonthView = null
  private val ResultSettings = 1
  private val exampleText =
    Array("晉穆帝永和九年", "晉穆帝永和九年三月", "晉穆帝永和九年三月初三", "晉穆帝永和九年三月丙辰", "353年4月22日")
  private val guideText = exampleText.mkString("\n")
  private var displayChinese = "simplified"
  
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
    val activity = this
    resultText = findViewById(R.id.result).asInstanceOf[TextView]
    resultText.setText(guideText)

    monthView = findViewById(R.id.month).asInstanceOf[MonthView]
    monthView.searchActivity = this

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
          val queryText = simplified2Traditional(s.toString())
          var chineseDateText = queryText
          var dateText = ""
          if (Character.isDigit(queryText.charAt(0))) {
            val result = fromDate(queryText)
            chineseDateText = result(0)            
            val resultNorm = 
              if (displayChinese == "simplified") result.map(traditional2Simplified(_))
              else result
            dateText = resultNorm.mkString("\n")
          } else {
            dateText = toDate(queryText).toString()
          }
          resultText.setText(dateText)
          Util.hideSoftInput(activity, searchEntry)
          showMonthView(parseDate(chineseDateText))
        } catch {
          case ex: Exception =>
            resultText.setText("......")
            monthView.showing = false
            monthView.invalidate()
        }
      }

      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
      }

      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
      }
    })
  }
  
  // Initialize the contents of the widgets.
  private def initContents() {
    val names = eraNames().map(s => s + "元年")
    displayChinese = Util.getChinesePref(this)
    var displayNames = names
    if (displayChinese == "simplified") {
      displayNames = names.map(s => traditional2Simplified(s))
    }

    searchEntry.setAdapter(new CalendarArrayAdapter(this, R.layout.simple_dropdown_item_1line,
      displayNames))
    searchEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        //
      }    
    })  
  }

  def showMonthView(chineseDate: ChineseCalendar) {
    val yearSexagenary = (if (displayChinese == "simplified") "岁次" else "歲次") +
                         chineseDate.yearSexagenary()
    monthView.chineseDate = chineseDate
    monthView.year = chineseDate.monarchEra + chineseDate.year + yearSexagenary
    monthView.month = chineseDate.month
    monthView.sexagenary1stDay = sexagenary1stDayOfMonth(chineseDate)
    monthView.daysPerMonth = monthLength(chineseDate)
    monthView.showing = true
    monthView.invalidate()
  }
}
