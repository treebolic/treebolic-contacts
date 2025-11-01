/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import org.treebolic.TreebolicIface
import treebolic.provider.contacts.Settings
import java.util.Properties

/**
 * Settings
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
internal object Settings {

    /**
     * Initialized preference key
     */
    const val PREF_INITIALIZED: String = "pref_initialized"

    /**
     * First preference key
     */
    const val PREF_FIRSTRUN: String = "pref_first_run"

    /**
     * Grouped preference key
     */
    const val PREF_GROUPED: String = "pref_grouped"

    /**
     * Display preference key
     */
    const val PREF_DISPLAY: String = "pref_display"

    /**
     * Sort preference key
     */
    const val PREF_SORT: String = "pref_sort"

    /**
     * Group merging preference key
     */
    const val PREF_MERGE_GROUPS: String = "pref_merge_groups"

    /**
     * Group filter preference key
     */
    const val PREF_FILTER_GROUPS: String = "pref_filter_groups"

    /**
     * Photo preference key
     */
    const val PREF_PHOTO: String = "pref_photo"

    /**
     * Default CSS
     */
    const val STYLE_DEFAULT: String = ".content { }\n" +  
            ".link {color: #FFA500;font-size: small;}\n" +  
            ".linking {color: #FFA500; font-size: small; }" +  
            ".mount {color: #CD5C5C; font-size: small;}" +  
            ".mounting {color: #CD5C5C; font-size: small; }" +  
            ".searching {color: #FF7F50; font-size: small; }"

    /**
     * Set default initial settings
     *
     * @param context context
     */
    @Suppress("EmptyMethod")
    @JvmStatic
    fun setDefaults(@Suppress("unused") context: Context?) {
        
    }

    /**
     * Save source and base to preferences
     *
     * @param context context
     * @param source  source
     */
    fun save(context: Context, source: String) {
        putStringPref(context, TreebolicIface.PREF_SOURCE, source)
    }

    /**
     * Get string preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
    fun getStringPref(context: Context, key: String?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString(key, null)
    }

    /**
     * Put string preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    private fun putStringPref(context: Context, @Suppress("SameParameterValue") key: String, value: String) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) { putString(key, value) }
    }

    /**
     * Get strings preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
    fun getStringsPref(context: Context, key: String?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val values = sharedPref.getStringSet(key, null)
        if (values.isNullOrEmpty()) {
            return null
        }
        return joinForIn(values)
    }

    /**
     * Get boolean preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    private fun getBooleanPref(context: Context, key: String?): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getBoolean(key, false)
    }

    /**
     * Join for 'in' selection clause
     *
     * @param strings values
     * @return 'in' clause
     */
    private fun joinForIn(strings: Iterable<String>): String {
        val sb = StringBuilder()
        var first = true
        sb.append('(')
        for (str in strings) {
            if (first) {
                first = false
            } else {
                sb.append(',')
            }
            sb.append(str)
        }
        sb.append(')')
        return sb.toString()
    }

    // U T I L S

    /**
     * Collect preferences to properties
     *
     * @param context context
     * @return properties
     */
    @JvmStatic
    fun settingsToProperties(context: Context): Properties {
        val source = getStringPref(context, TreebolicIface.PREF_SOURCE)
        val groups = getBooleanPref(context, PREF_GROUPED)
        val mergeGroups = getBooleanPref(context, PREF_MERGE_GROUPS)
        val photo = getBooleanPref(context, PREF_PHOTO)
        val display = getStringPref(context, PREF_DISPLAY)
        val sort = getStringPref(context, PREF_SORT)
        val filterGroups = getStringsPref(context, PREF_FILTER_GROUPS)

        val properties = Properties()
        if (source != null) {
            properties.setProperty(TreebolicIface.PREF_SOURCE, source)
        }
        properties.setProperty(Settings.PARAM_GROUPS, if (groups) "true" else "false")
        properties.setProperty(Settings.PARAM_MERGE_GROUPS, if (mergeGroups) "true" else "false")
        properties.setProperty(Settings.PARAM_PHOTO, if (photo) "true" else "false")
        if (display != null) {
            properties.setProperty(Settings.PARAM_DISPLAY, display)
        }
        if (sort != null) {
            properties.setProperty(Settings.PARAM_SORT, sort)
        }
        if (filterGroups != null) {
            properties.setProperty(Settings.PARAM_FILTER_GROUPS, filterGroups)
        }
        return properties
    }

    /**
     * Application settings
     *
     * @param context context
     * @param pkgName package name
     */
    @JvmStatic
    fun applicationSettings(context: Context, pkgName: String) {
        val apiLevel = Build.VERSION.SDK_INT
        val intent = Intent()

        if (apiLevel >= 9) {
            intent.action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = "package:$pkgName".toUri()
        } else {
            val appPkgName = if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"

            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, pkgName)
        }

        // start activity
        context.startActivity(intent)
    }
}
