/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import org.treebolic.AppCompatCommonActivity
import org.treebolic.makeDialog
import treebolic.model.ModelDump
import treebolic.provider.contacts.Provider

/**
 * Abstract activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
abstract class AbstractActivity : AppCompatCommonActivity() {

    abstract fun makeFragment(): Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // layout
        setContentView(R.layout.activity_fragment_container)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_HOME_AS_UP
        }

        // fragment
        if (savedInstanceState == null) {
            val fragment = makeFragment()
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (R.id.action_model == id) {
            val provider = Provider()
            provider.setHandle(this)
            val model = provider.makeModel(null, null, null)
            val dump = ModelDump.toString(model)
            makeDialog(dump, this).show()
            return true
        } else if (R.id.action_settings == id) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}
