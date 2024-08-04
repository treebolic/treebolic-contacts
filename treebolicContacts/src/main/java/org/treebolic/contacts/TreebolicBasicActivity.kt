/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.ContactsContract
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.multidex.BuildConfig
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import org.treebolic.AppCompatCommonActivity
import org.treebolic.TreebolicIface
import org.treebolic.guide.AboutActivity
import org.treebolic.guide.HelpActivity
import org.treebolic.guide.Tip
import org.treebolic.search.ColorUtils.getActionBarForegroundColorFromTheme
import org.treebolic.search.ColorUtils.tint
import org.treebolic.search.SearchSettings
import treebolic.IContext
import treebolic.Widget
import treebolic.glue.component.Container
import treebolic.glue.component.Surface
import treebolic.glue.component.Utils
import java.net.URL
import java.util.Properties

/**
 * Treebolic basic activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
abstract class TreebolicBasicActivity
protected constructor(
    private val menuId: Int
) : AppCompatCommonActivity(), IContext {

    /**
     * Parameter : Document base
     */
    private var base: String? = null

    /**
     * Parameter : Image base
     */
    private var imagesBase: String? = null

    /**
     * Parameter : Settings
     */
    protected var settings: String? = null

    /**
     * Parameter : CSS style for WebViews
     */
    private var cssStyle: String? = null

    override fun getStyle(): String? {
        return if (this.cssStyle != null) this.cssStyle else  //
            Settings.STYLE_DEFAULT
    }

    /**
     * Parameter : more parameters
     */
    private var more: Bundle? = null

    /**
     * Derived parameter : parameters
     */
    private var parameters: Properties? = null

    override fun getParameters(): Properties {
        return parameters!!
    }

    // components
    /**
     * Treebolic widget
     */
    @JvmField
    protected var widget: Widget? = null

    /**
     * Search view on action bar
     */
    protected var searchView: SearchView? = null

    /**
     * Input
     */
    private val input: String? = null

    // parent

    /**
     * Parent (client) activity
     */
    private var parentActivityIntentArg: Intent? = null

    override fun getParentActivityIntent(): Intent? {
        if (parentActivityIntentArg != null) {
            return parentActivityIntentArg
        }
        return super.getParentActivityIntent()
    }

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup
        val value = TypedValue()
        resources.getValue(R.dimen.splitter_position_percent, value, true)
        val splitterPositionPercent = value.float
        Container.splitterPositionPercent = splitterPositionPercent

        // widget
        this.widget = Widget(this, this)

        // content view
        setContentView(R.layout.activity_treebolic)
        val container = findViewById<ViewGroup>(R.id.container)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        val view: View = widget as View
        container.addView(view, params)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // action bar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.elevation = 0f
            actionBar.displayOptions = ActionBar.DISPLAY_USE_LOGO or ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
        }
    }

    override fun onNewIntent(intent: Intent) {
        // an activity will always be paused before receiving a new intent, so you can count on onResume() being called after this method
        super.onNewIntent(intent)

        // getIntent() still returns the original Intent, use setIntent(Intent) to update it to this new Intent.
        setIntent(intent)
    }

    override fun onResume() {
        Log.d(TAG, "Activity resumed")

        // super
        super.onResume()

        // retrieve arguments
        unmarshalArgs(intent)

        // make parameters
        this.parameters = makeParameters()

        // query
        query()

        // first run
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val hasRun = prefs.getBoolean(Settings.PREF_FIRSTRUN, false)
        if (!hasRun) {
            val edit = prefs.edit()

            // flag as 'has run'
            edit.putBoolean(Settings.PREF_FIRSTRUN, true).apply()

            // tips
            Tip.show(supportFragmentManager)
        }
    }

    override fun onPause() {
        Log.d(TAG, "Activity paused, terminating surface drawing thread")

        // terminate thread
        val surface: Surface? = widget?.view
        surface?.thread?.terminate()

        // super
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // inflate
        menuInflater.inflate(this.menuId, menu)

        // search view
        val searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.expandActionView()
        this.searchView = searchMenuItem.actionView as SearchView?

        // search view width
        val screenWidth = Utils.screenWidth(this)
        searchView!!.maxWidth = screenWidth / 2

        // search view listener
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView!!.clearFocus()
                searchView!!.setQuery("", false)
                handleQueryChanged(query, true)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                handleQueryChanged(query, false)
                return true
            }
        })

        // icon tint
        val iconTint = getActionBarForegroundColorFromTheme(this)
        tint(iconTint, menu, R.id.action_search_run, R.id.action_search_reset, R.id.action_search_settings)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (R.id.action_filter == id) {
            val intent = Intent(this, FilterActivity::class.java)
            startActivity(intent)
            return true
        } else if (R.id.action_finish == id) {
            finish()
            return true
        } else if (R.id.action_kill == id) {
            Process.killProcess(Process.myPid())
            return true
        } else if (R.id.action_tips == id) {
            Tip.show(supportFragmentManager)
            return true
        } else if (R.id.action_help == id) {
            startActivity(Intent(this, HelpActivity::class.java))
            return true
        } else if (R.id.action_about == id) {
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        } else if (R.id.action_search_run == id) {
            handleSearchRun()
            return true
        } else if (R.id.action_search_reset == id) {
            handleSearchReset()
            return true
        } else if (R.id.action_search_settings == id) {
            SearchSettings.show(supportFragmentManager)
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
        } else if (R.id.action_settings == id) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return false
    }

   // T R E E B O L I C   M O D E L

    /**
     * Unmarshal model and parameters from intent
     *
     * @param intent intent
     */
    protected open fun unmarshalArgs(intent: Intent) {
        // retrieve arguments
        val params = checkNotNull(intent.extras)
        params.classLoader = classLoader

        // retrieve arguments
        this.base = params.getString(TreebolicIface.ARG_BASE)
        this.imagesBase = params.getString(TreebolicIface.ARG_IMAGEBASE)
        this.settings = params.getString(TreebolicIface.ARG_SETTINGS)
        this.cssStyle = params.getString(TreebolicIface.ARG_STYLE)
        @Suppress("DEPRECATION")
        this.parentActivityIntentArg = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
            params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY, Intent::class.java) else  //
            params.getParcelable(TreebolicIface.ARG_PARENTACTIVITY)
        @Suppress("DEPRECATION")
        this.more = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) //
            params.getParcelable(TreebolicIface.ARG_MORE, Bundle::class.java) else  //
            params.getParcelable(TreebolicIface.ARG_MORE)
    }

    // T R E E B O L I C   C O N T E X T

    override fun getBase(): URL? {
        return null
    }

    override fun getImagesBase(): URL? {
        return null
    }

    override fun linkTo(url: String, target: String): Boolean {
        // if url is handled by client, return query to client, which will handle it by initiating another query
        if (url.startsWith(treebolic.provider.contacts.Settings.URLSCHEME)) {
            val contactId = url.substring(treebolic.provider.contacts.Settings.URLSCHEME.length)
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId)
            intent.setData(uri)
            startActivity(intent)
            return true
        }

        // standard handling
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse(url)
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mimetype == null) {
                intent.setData(uri)
            } else {
                intent.setDataAndType(uri, mimetype)
            }
            startActivity(intent)
            return true
        } catch (ignored: Exception) {
            Toast.makeText(this, R.string.error_link, Toast.LENGTH_LONG).show()
        }
        return false
    }

    override fun getInput(): String? {
        return this.input
    }

    override fun warn(message: String) {
        // toast(message, Toast.LENGTH_LONG);
        snackbar(message, Snackbar.LENGTH_LONG)
    }

    override fun status(message: String) {
        // toast(message, Toast.LENGTH_SHORT);
        snackbar(message, Snackbar.LENGTH_SHORT)
    }

    // Q U E R Y

    /**
     * Initial query
     */
    protected abstract fun query()

    /**
     * Requery (linkTo, or searchView)
     *
     * @param source source
     */
    protected abstract fun requery(source: String?)

    /**
     * Search pending flag
     */
    private var searchPending = false

    /**
     * SearchView query change listener
     *
     * @param query  new query
     * @param submit whether submit was changed
     */
    protected fun handleQueryChanged(query: String, submit: Boolean) {
        // clear keyboard out of the way
        if (submit) {
            closeKeyboard()
        }

        // reset current search if any
        resetSearch()

        if (submit /*|| query.length() > SEARCH_TRIGGER_LEVEL*/) {
            // query applies to source: search is a requery
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL) // label, content, link, id
            if (SearchSettings.SCOPE_SOURCE == scope) {
                Log.d(TAG, "Source \"$query\"")
                //if (submit)
                //{
                requery(query)
                //}
                return
            }

            // query applies to non-source scope (label, content, ..): tree search
            val mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH) // equals, startswith, includes
            runSearch(scope, mode, query)
        }
    }

    /**
     * Tree search handler
     */
    private fun handleSearchRun() {
        // clear keyboard out of the way
        closeKeyboard()

        // new or continued search
        if (!this.searchPending) {
            val query = searchView!!.query.toString()
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
            val scope = sharedPrefs.getString(SearchSettings.PREF_SEARCH_SCOPE, SearchSettings.SCOPE_LABEL) // label, content, link, id
            if (SearchSettings.SCOPE_SOURCE == scope) {
                Log.d(TAG, "Source \"$query\"")
                requery(query)
                return
            }

            val mode = sharedPrefs.getString(SearchSettings.PREF_SEARCH_MODE, SearchSettings.MODE_STARTSWITH) // equals, startswith, includes
            runSearch(scope, mode, query)
        } else {
            continueSearch()
        }
    }

    /**
     * Tree search reset handler
     */
    private fun handleSearchReset() {
        // clear keyboard out of the way
        closeKeyboard()

        // get query
        val query = searchView!!.query.toString()

        // clear current query
        searchView!!.setQuery("", false)

        // query was already empty
        if (query.isEmpty()) {
            resetSearch()
        }

        // home
        widget!!.focus(null)
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = checkNotNull(getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // S E A R C H   I N T E R F A C E

    private fun runSearch(scope: String?, mode: String?, target: String?) {
        if (target.isNullOrEmpty()) {
            return
        }

        Log.d(TAG, "Search run$scope $mode $target")
        this.searchPending = true
        widget!!.search(CMD_SEARCH, scope, mode, target)
    }

    private fun continueSearch() {
        Log.d(TAG, "Search continue")
        widget!!.search(CMD_CONTINUE)
    }

    private fun resetSearch() {
        Log.d(TAG, "Search reset")
        this.searchPending = false
        widget!!.search(CMD_RESET)
    }

    // H E L P E R S

    /**
     * Make parameters from bundle
     *
     * @return properties
     */
    protected open fun makeParameters(): Properties? {
        val parameters = Properties()
        if (this.base != null) {
            parameters.setProperty("base", this.base)
        }
        if (this.imagesBase != null) {
            parameters.setProperty("imagebase", this.imagesBase)
        }
        if (this.settings != null) {
            parameters.setProperty("settings", this.settings)
        }

        // add properties from bundle
        if (this.more != null) {
            for (key in more!!.keySet()) {
                val value = more!!.getString(key)
                if (value != null) {
                    parameters.setProperty(key, value)
                }
            }
        }

        parameters.setProperty("debug", BuildConfig.DEBUG.toString())
        return parameters
    }

    /**
     * Put snackbar on UI thread
     *
     * @param message  message
     * @param duration duration
     */
    private fun snackbar(message: String, duration: Int) {
        runOnUiThread {
            val snack: Snackbar = Snackbar.make(widget as View, message, duration)
            snack.view.setBackgroundColor(ContextCompat.getColor(this@TreebolicBasicActivity, R.color.snackbar_color))
            snack.show()
        }
    }

    companion object {

        private const val TAG = "TreebolicBasicA"

        // S E A R C H

        private const val CMD_SEARCH = "SEARCH"

        private const val CMD_RESET = "RESET"

        private const val CMD_CONTINUE = "CONTINUE"

        private const val SEARCH_TRIGGER_LEVEL = Int.MAX_VALUE
    }
}
