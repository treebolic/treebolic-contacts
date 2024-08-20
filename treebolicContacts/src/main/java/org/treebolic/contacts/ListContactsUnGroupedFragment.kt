/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.app.Activity
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import org.treebolic.TreebolicIface
import org.treebolic.contacts.Settings.getStringPref
import org.treebolic.contacts.Settings.getStringsPref

/**
 * Fragment with list of contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListContactsUnGroupedFragment : AbstractListContactsCursorFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Adapter that binds the result Cursor to the ListView
     */
    private var adapter: SimpleCursorAdapter? = null

    // O V E R R I D E S

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this)

        // Gets adapter's source columns
        val displayPref = getStringPref(requireContext(), Settings.PREF_DISPLAY)
        val firstLastValue = getString(R.string.pref_display_value_first_last)
        val displayCode = if (firstLastValue == displayPref) 1 else 2
        val from: Array<String> = if (displayCode == 1) FROM_COLUMNS else FROM_COLUMNS_ALT

        // Gets a CursorAdapter
        val activity: Activity = requireActivity()
        this.adapter = ImageCursorAdapter(activity, R.layout.item_contact_ungrouped, null, from, TO_IDS, 0)

        // Gets the ListView from the View list of the parent activity
        val contactsListView = activity.findViewById<ListView>(android.R.id.list)

        // Sets the adapter for the ListView
        contactsListView.adapter = adapter
    }

    // L O A D E R

    override fun onCreateLoader(loaderId: Int, args: Bundle?): Loader<Cursor> {
        // selection
        val source = getStringPref(requireContext(), TreebolicIface.PREF_SOURCE)
        val nullSource = source.isNullOrEmpty()
        var selection = if (nullSource) null else treebolic.provider.contacts.Settings.SELECTION_DN_LIKE
        val selectionArgs = if (nullSource) null else arrayOf("%$source%")
        var extraSelection = ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'"
        val groups = getStringsPref(requireContext(), Settings.PREF_FILTER_GROUPS)
        if (groups != null) {
            extraSelection += " AND " + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groups
        }
        selection = if (selection == null) extraSelection else "$selection AND $extraSelection"

        // sort
        val sortPref = getStringPref(requireContext(), Settings.PREF_SORT)
        val firstValue = getString(R.string.pref_sort_value_first)
        val sortCode = if (firstValue == sortPref) 1 else 2
        var sort = if (sortCode == 1) ContactsContract.Contacts.DISPLAY_NAME else ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC"
        val extraSort = ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID
        sort = "$sort,$extraSort"

        Log.d(TAG, "selection=$selection")
        val activity: Activity = requireActivity()
        return CursorLoader(
            activity,  //
            treebolic.provider.contacts.Settings.GROUP_MEMBERS_URI,  //
            treebolic.provider.contacts.Settings.GROUP_MEMBERS_PROJECTION,  //
            selection,  //
            selectionArgs,  //
            sort
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        Log.d(TAG, "count=" + cursor.count)

        // Put the result Cursor in the adapter for the ListView
        adapter!!.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Delete the reference to the existing Cursor
        adapter!!.swapCursor(null)
    }

    companion object {

        private const val TAG = "ListContactsUnGroupedF"

        // A D A P T E R

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val FROM_COLUMNS: Array<String>

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val FROM_COLUMNS_ALT: Array<String>

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val TO_IDS: IntArray

        init {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                FROM_COLUMNS = arrayOf(
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,  //
                    ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET
                )
                FROM_COLUMNS_ALT = arrayOf(
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,  //
                    ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET
                )
                TO_IDS = intArrayOf(R.id.display_name, R.id.group, R.id.account)
            } else {
                FROM_COLUMNS = arrayOf(
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                )
                FROM_COLUMNS_ALT = arrayOf(
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                )
                TO_IDS = intArrayOf(R.id.display_name, R.id.group)
            }
        }
    }
}