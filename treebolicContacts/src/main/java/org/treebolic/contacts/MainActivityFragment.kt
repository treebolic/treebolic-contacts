/*
 * Copyright (c) 2026. Bernard Bou
 */
package org.treebolic.contacts

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.treebolic.TreebolicIface
import org.treebolic.contacts.Settings.getStringPref
import org.treebolic.contacts.Settings.settingsToProperties
import treebolic.provider.contacts.Permissions.check
import treebolic.provider.contacts.Provider.Companion.count
import treebolic.provider.contacts.Provider.Companion.countContacts
import treebolic.provider.contacts.Provider.Companion.countGroups

/**
 * Main activity fragment
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class MainActivityFragment : Fragment() {

    private lateinit var statusView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        statusView = view.findViewById(R.id.contactStatus)
        return view
    }

    override fun onResume() {
        super.onResume()
        val activity: Activity = requireActivity()
        if (check(activity)) {
            activity.finish()
            return
        }
        val resources = activity.resources
        val source = getStringPref(activity, TreebolicIface.PREF_SOURCE)
        val parameters = settingsToProperties(requireContext())
        val count = count(activity, source, parameters).toInt()
        val countContacts = countContacts(activity, source).toInt()
        val countGroups = countGroups(activity).toInt()
        val countStr = resources.getQuantityString(R.plurals.format_count, count, count)
        val countContactsStr = resources.getQuantityString(R.plurals.format_count_contacts, countContacts, countContacts)
        val countGroupsStr = resources.getQuantityString(R.plurals.format_count_groups, countGroups, countGroups)
        val status = resources.getString(R.string.status, countContactsStr, countGroupsStr, countStr)

        statusView.text = status
    }
}