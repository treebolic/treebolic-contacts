/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.os.Bundle
import android.widget.ListAdapter

class GroupPreferenceDialogFragmentCompat : MultiSelectListPreferenceCustomDialogFragmentCompat() {

    private val accountNames: MutableList<String?> = ArrayList()

    private val accountTypes: MutableList<String?> = ArrayList()

    private val counts: MutableList<Long> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preference = preference as GroupPreference
        accountNames.clear()
        accountNames.addAll(preference.accountNames)
        accountTypes.clear()
        accountTypes.addAll(preference.accountTypes)
        counts.clear()
        counts.addAll(preference.counts)
    }

    override val adapter: ListAdapter
        get() {
            val entryCount = if (entryValues == null) 0 else entryValues!!.size
            val data = List(entryCount) {
                checkNotNull(entries)
                val checked = newValues.contains(entryValues!![it].toString())
                val row: MutableMap<String, Any?> = HashMap()
                row["checkAndRequest"] = checked
                row["group_id"] = entryValues!![it].toString()
                row["display_name"] = entries!![it]
                row["account_name"] = accountNames[it]
                row["account_type"] = accountTypes[it]
                row["count"] = counts[it]
                row
            }

            val from = arrayOf("checkAndRequest", "group_id", "display_name", "account_name", "account_type", "count")
            val to = intArrayOf(android.R.id.checkbox, R.id.group_id, R.id.display_name, R.id.account_name, R.id.account_type, R.id.count)
            return CheckBoxSimpleAdapter(context, data, R.layout.item_group_select, from, to, android.R.id.checkbox)
        }

    companion object {

        fun newInstance(pref: GroupPreference): MultiSelectListPreferenceCustomDialogFragmentCompat {
            val fragment = GroupPreferenceDialogFragmentCompat()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, pref.key)
            fragment.arguments = bundle
            return fragment
        }
    }
}
