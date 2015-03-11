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
import net.whily.chinesecalendar.ChineseCalendar._
import net.whily.chinesecalendar.Chinese._

class SearchActivity extends Activity {
  private var bar: ActionBar = null
  private var searchEntry: AutoCompleteTextView = null
  private var clearButton: Button = null
  private var resultText: TextView = null  
  private val ResultSettings = 1
  private val exampleText =
    Array("晉穆帝永和九年", "晉穆帝永和九年三月", "晉穆帝永和九年三月初三", "晉穆帝永和九年三月丙辰", "353年4月22日")
  private val guideText = exampleText.mkString("\n")
  
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
    resultText = findViewById(R.id.result).asInstanceOf[TextView]
    resultText.setText(guideText)

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
          resultText.setText(toDate(Simplified2Traditional(s.toString())).toString())
        } catch {
          case ex: Exception => resultText.setText("......")
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
    searchEntry.setAdapter(new CalendarArrayAdapter(this, R.layout.simple_dropdown_item_1line,
      exampleText))
    searchEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        //
      }    
    })  
  }
}
