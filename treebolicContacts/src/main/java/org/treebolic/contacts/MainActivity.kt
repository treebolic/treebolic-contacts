/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity
import com.bbou.rate.AppRate.invoke
import com.bbou.rate.AppRate.rate
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.contacts.Settings.applicationSettings
import org.treebolic.contacts.Settings.getStringsPref
import org.treebolic.contacts.Settings.setDefaults
import org.treebolic.guide.AboutActivity
import org.treebolic.guide.HelpActivity
import org.treebolic.guide.Tip.Companion.show
import treebolic.model.ModelDump
import treebolic.provider.contacts.Permissions.checkAndRequest
import treebolic.provider.contacts.Provider
import treebolic.provider.contacts.Settings.getBooleanPref
import treebolic.provider.contacts.Settings.getStringPref

/**
 * Main activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class MainActivity : AppCompatCommonActivity() {

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // rate
        invoke(this)

        // layout
        setContentView(R.layout.activity_main)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE
        }

        // initialize
        initialize()

        // fragment
        if (savedInstanceState == null) {
            val fragment: Fragment = MainActivityFragment()
            supportFragmentManager.beginTransaction().add(R.id.container, fragment).commit()
        }
    }

    override fun onResume() {
        updateButton()
        super.onResume()
    }

    // H E L P E R

    private fun updateButton() {
        val button = findViewById<ImageButton>(R.id.queryButton)
        button.setOnClickListener { view: View? -> this.onClick(view) }
        // final String source = Settings.getStringPref(this, TreebolicIface.PREF_SOURCE);
        button.visibility = View.VISIBLE
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (R.id.action_run == id) {
            query()
            return true
        } else if (R.id.action_list_contacts == id) {
            val intent = Intent(this, ListContactsActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_list_grouped_contacts == id) {
            val intent = Intent(this, ListContactsGroupedActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_list_ungrouped_contacts == id) {
            val intent = Intent(this, ListContactsUnGroupedActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_list_groups == id) {
            val intent = Intent(this, ListGroupsActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_model == id) {
            val provider = Provider()
            provider.setHandle(this)
            val model = provider.makeModel(null, null, null)
            val dump = ModelDump.toString(model)
            //System.out.println(dump);
            val builder = AlertDialog.Builder(this)
            builder.setMessage(dump)
            builder.show()
            return true
        } else if (R.id.action_filter == id) {
            val intent = Intent(this, FilterActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_settings == id) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_help == id) {
            startActivity(Intent(this, HelpActivity::class.java))
            return true
        } else if (R.id.action_tips == id) {
            show(supportFragmentManager)
            return true
        } else if (R.id.action_about == id) {
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        } else if (R.id.action_others == id) {
            startActivity(Intent(this, OthersActivity::class.java))
            return true
        } else if (R.id.action_donate == id) {
            startActivity(Intent(this, DonateActivity::class.java))
            return true
        } else if (R.id.action_rate == id) {
            rate(this)
            return true
        } else if (R.id.action_app_settings == id) {
            applicationSettings(this, "org.treebolic.contacts")
            return true
        } else if (R.id.action_finish == id) {
            finish()
            return true
        } else if (R.id.action_kill == id) {
            Process.killProcess(Process.myPid())
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    // I N I T I A L I Z E

    /**
     * Initialize
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun initialize() {
        // permissions
        checkAndRequest(this)

        // test if initialized
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val initialized = sharedPref.getBoolean(Settings.PREF_INITIALIZED, false)
        if (!initialized) {
            // default settings
            setDefaults(this)

            // flag as initialized
            sharedPref.edit().putBoolean(Settings.PREF_INITIALIZED, true).commit()
        }
    }

    // Q U E R Y

    private fun query() {
        tryStartTreebolic(this)
    }

    // C L I C K

    /**
     * Click listener
     *
     * @param view view
     */
    private fun onClick(view: View?) {
        query()
    }

    companion object {

        private const val TAG = "MainActivity"

        /**
         * Start Treebolic plugin activity from source + base
         *
         * @param context context
         */
        private fun tryStartTreebolic(context: Context) {
            val intent = makeTreebolicIntent(context)
            Log.d(TAG, "Start treebolic")
            context.startActivity(intent)
        }

        /**
         * Make Treebolic intent
         *
         * @param context context
         * @return intent
         */
        private fun makeTreebolicIntent(context: Context): Intent {
            val source = getStringPref(context, TreebolicIface.PREF_SOURCE)
            val groups = getBooleanPref(context, Settings.PREF_GROUPED)
            val mergeGroups = getBooleanPref(context, Settings.PREF_MERGE_GROUPS)
            val photo = getBooleanPref(context, Settings.PREF_PHOTO)
            val display = getStringPref(context, Settings.PREF_DISPLAY)
            val sort = getStringPref(context, Settings.PREF_SORT)
            val filterGroups = getStringsPref(context, Settings.PREF_FILTER_GROUPS)
            val bundle = Bundle()
            bundle.putBoolean(treebolic.provider.contacts.Settings.PARAM_GROUPS, groups)
            bundle.putBoolean(treebolic.provider.contacts.Settings.PARAM_MERGE_GROUPS, mergeGroups)
            bundle.putBoolean(treebolic.provider.contacts.Settings.PARAM_PHOTO, photo)
            bundle.putString(treebolic.provider.contacts.Settings.PARAM_DISPLAY, display)
            bundle.putString(treebolic.provider.contacts.Settings.PARAM_SORT, sort)
            bundle.putString(treebolic.provider.contacts.Settings.PARAM_FILTER_GROUPS, filterGroups)

            // parent activity to return to
            val parentIntent = Intent()
            parentIntent.setClass(context, MainActivity::class.java)

            // intent
            val intent = Intent(context, TreebolicActivity::class.java)

            // model passing
            intent.putExtra(TreebolicIface.ARG_PROVIDER, "treebolic.provider.contacts.Provider")
            intent.putExtra(TreebolicIface.ARG_SOURCE, source)

            // other parameters passing
            intent.putExtra(TreebolicIface.ARG_BASE, null as String?)
            intent.putExtra(TreebolicIface.ARG_IMAGEBASE, null as String?)
            intent.putExtra(TreebolicIface.ARG_SETTINGS, null as String?)
            intent.putExtra(TreebolicIface.ARG_MORE, bundle)

            // parent passing
            intent.putExtra(TreebolicIface.ARG_PARENTACTIVITY, parentIntent)

            // scheme
            intent.putExtra(TreebolicIface.ARG_URLSCHEME, "contacts:")

            return intent
        }
    }
}
