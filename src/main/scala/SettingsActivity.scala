/**
 * Settings activity for World Metro.
 *
 * @author  Yujian Zhang <yujian{dot}zhang[at]gmail(dot)com>
 *
 * License: 
 *   GNU General Public License v2
 *   http://www.gnu.org/licenses/gpl-2.0.html
 * Copyright (C) 2015 Yujian Zhang
 */

package net.whily.android.calendarlookup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.{PreferenceActivity, PreferenceCategory}

class SettingsActivity extends PreferenceActivity {
  override def onCreate(savedInstanceState: Bundle) {
    // For PreferenceActivity, setTheme should proceed super.oCreate. 
    // See http://stackoverflow.com/questions/11751498/how-to-change-preferenceactivity-theme
    Misc.setMaterialTheme(this) 
    super.onCreate(savedInstanceState)
    
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.settings)
  }
}
