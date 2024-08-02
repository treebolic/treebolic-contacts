/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.treebolic.AppCompatCommonActivity
import org.treebolic.contacts.SettingsActivity.FilterPreferenceFragment

class FilterActivity : AppCompatCommonActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_sub_settings)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // show the Up button in the action bar.
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }

        // fragment
        if (savedInstanceState == null) {
            val fragment: Fragment = FilterPreferenceFragment()
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }
}
