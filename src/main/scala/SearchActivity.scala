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
import android.view.{KeyEvent, Menu, MenuItem, MotionEvent, View}
import android.view.inputmethod.InputMethodManager
import android.util.{Log, TypedValue}
import android.widget.{AdapterView, ArrayAdapter, AutoCompleteTextView, ExpandableListView, SimpleAdapter, TextView}
import net.whily.scaland.{ExceptionHandler, Util}
import net.whily.chinesecalendar.ChineseCalendar._

class SearchActivity extends Activity {
  private var bar: ActionBar = null
  private var searchEntry: AutoCompleteTextView = null
  private var resultText: TextView = null  
  private val ResultSettings = 1
  
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
    val editTextSize = Util.getEditTextSize(this)

    resultText = findViewById(R.id.result).asInstanceOf[TextView]
    resultText.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)

    searchEntry = findViewById(R.id.search_entry).asInstanceOf[AutoCompleteTextView]
    searchEntry.setThreshold(1)
    searchEntry.setTextSize(TypedValue.COMPLEX_UNIT_SP, editTextSize)
    searchEntry.setOnTouchListener(new View.OnTouchListener() {
      override def onTouch(v: View, e: MotionEvent): Boolean = {
      	searchEntry.showDropDown()
      	false
      }
    })
    searchEntry.setOnKeyListener(new View.OnKeyListener() {
      override def onKey(v: View,  keyCode: Int, event: KeyEvent): Boolean = {
        val searchText = searchEntry.getText().toString
        try {
          resultText.setText(toDate(searchText).toString())
        } catch {
          case ex: Exception => None
        }
        true
      }
    })
  }
  
  // Initialize the contents of the widgets.
  private def initContents() {
    searchEntry.setAdapter(new CalendarArrayAdapter(this, R.layout.simple_dropdown_item_1line, Array("123", "445")))
    searchEntry.setOnItemClickListener(new AdapterView.OnItemClickListener () {
      override def onItemClick(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        //
      }    
    })  
  }
}
