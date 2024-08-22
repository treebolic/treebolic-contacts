/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import androidx.appcompat.app.AlertDialog
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceDialogFragmentCompat

abstract class MultiSelectListPreferenceCustomDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    val newValues: MutableSet<String> = HashSet()
    var preferenceChanged: Boolean = false
    var entries: Array<CharSequence>? = null
    var entryValues: Array<CharSequence>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val preference = listPreference
            check(!(preference.entries == null || preference.entryValues == null)) { "MultiSelectListPreference requires an entries array and " + "an entryValues array." }

            newValues.clear()
            newValues.addAll(preference.values)
            preferenceChanged = false
            entries = preference.entries
            entryValues = preference.entryValues
        } else {
            newValues.clear()
            val valueList: List<String>? = savedInstanceState.getStringArrayList(SAVE_STATE_VALUES)
            if (valueList != null) {
                newValues.addAll(valueList)
            }
            preferenceChanged = savedInstanceState.getBoolean(SAVE_STATE_CHANGED, false)
            entries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES)
            entryValues = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(SAVE_STATE_VALUES, ArrayList(newValues))
        outState.putBoolean(SAVE_STATE_CHANGED, preferenceChanged)
        outState.putCharSequenceArray(SAVE_STATE_ENTRIES, entries)
        outState.putCharSequenceArray(SAVE_STATE_ENTRY_VALUES, entryValues)
    }

    private val listPreference: MultiSelectListPreference
        get() = preference as MultiSelectListPreference

    protected abstract val adapter: ListAdapter?

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        val listAdapter = adapter
        builder.setAdapter(listAdapter, null)
    }

    internal inner class CheckBoxSimpleAdapter(context: Context?, data: List<MutableMap<String, *>?>?, resource: Int, from: Array<String>?, to: IntArray?, private val checkId: Int) : SimpleAdapter(context, data, resource, from, to) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)

            val checkBox = view.findViewById<CheckBox>(checkId)
            checkBox.tag = position
            checkBox.setOnClickListener { v: View ->
                if (entryValues == null) {
                    return@setOnClickListener
                }
                val checkBox2 = v as CheckBox
                val position2 = checkBox2.tag as Int
                val isChecked = checkBox2.isChecked
                preferenceChanged = if (isChecked) {
                    preferenceChanged or newValues.add(entryValues!![position2].toString())
                } else {
                    preferenceChanged or newValues.remove(entryValues!![position2].toString())
                }
            }

            return view
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult && preferenceChanged) {
            val preference = listPreference
            if (preference.callChangeListener(newValues)) {
                preference.values = newValues
            }
        }
        preferenceChanged = false
    }

    companion object {

        private const val SAVE_STATE_VALUES = "MultiSelectListPreferenceDialogFragmentCompat.values"
        private const val SAVE_STATE_CHANGED = "MultiSelectListPreferenceDialogFragmentCompat.changed"
        private const val SAVE_STATE_ENTRIES = "MultiSelectListPreferenceDialogFragmentCompat.entries"
        private const val SAVE_STATE_ENTRY_VALUES = "MultiSelectListPreferenceDialogFragmentCompat.entryValues"
    }
}