/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.TreebolicIface

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
object SettingsActivity : AppCompatCommonPreferenceActivity() {

    // S U M M A R Y

    /**
     * A list preference summary provider.
     */
    private val LIST_SUMMARY_PROVIDER = SummaryProvider { preference: MultiSelectListPreference ->
        val context = preference.context
        val header: CharSequence = context.getString(R.string.pref_description_group_filter)
        val values = preference.getPersistedStringSet(null)
        var summary = header.toString() + '\n'
        if (values == null || values.isEmpty()) {
            summary += context.getString(R.string.all)
        } else {
            summary += setToString(values)
        }
        summary
    }

    private fun setToString(values: Set<String>?): CharSequence {
        if (values == null) {
            return ""
        }
        val sb = StringBuilder()
        var isFirst = true
        for (value in values) {
            if (isFirst) {
                isFirst = false
            } else {
                sb.append(' ')
            }
            sb.append(value)
        }
        return sb
    }

    // F R A G M E N T S

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_general)

            // bind
            val displayPref = checkNotNull(findPreference<ListPreference>(Settings.PREF_DISPLAY))
            displayPref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

            val sortPref = checkNotNull(findPreference<ListPreference>(Settings.PREF_SORT))
            sortPref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }

    class FilterPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_filter)

            // bind
            val sourcePref = checkNotNull(findPreference<EditTextPreference>(TreebolicIface.PREF_SOURCE))
            sourcePref.summaryProvider = SOURCE_SUMMARY_PROVIDER

            val pref = checkNotNull(findPreference<MultiSelectListPreference>(Settings.PREF_FILTER_GROUPS))
            pref.summaryProvider = LIST_SUMMARY_PROVIDER
        }

        override fun onDisplayPreferenceDialog(pref: Preference) {
            val manager: FragmentManager
            try {
                manager = parentFragmentManager
            } catch (e: IllegalStateException) {
                return
            }

            if (manager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return
            }

            if (pref is GroupPreference) {
                val dialogFragment: DialogFragment = GroupPreferenceDialogFragmentCompat.newInstance(pref)
                @Suppress("DEPRECATION")
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(manager, DIALOG_FRAGMENT_TAG)
                return
            }
            super.onDisplayPreferenceDialog(pref)
        }

        companion object {

            /**
             * Source summary provider.
             */
            private val SOURCE_SUMMARY_PROVIDER = SummaryProvider { preference: EditTextPreference ->
                val context = preference.context
                val prefs = checkNotNull(preference.sharedPreferences)
                val desc = context.getString(R.string.pref_description_source)
                var value = prefs.getString(preference.key, null)
                value = if (value.isNullOrEmpty()) {
                    context.getString(R.string.none)
                } else {
                    "'$value'"
                }
                "$desc :\n$value"
            }

            private const val DIALOG_FRAGMENT_TAG = "GroupPreference"
        }
    }
}
