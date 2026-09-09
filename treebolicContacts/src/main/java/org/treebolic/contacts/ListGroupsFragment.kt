/*
 * Copyright (c) 2026. Bernard Bou
 */
package org.treebolic.contacts

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.provider.ContactsContract
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import org.treebolic.contacts.AbstractListContactsFragment.XSpannableStringBuilder
import org.treebolic.contacts.Settings.getStringsPref
import org.treebolic.makeDialog

/**
 * Fragment with list of contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListGroupsFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    /**
     * Adapter that binds the result Cursor to the ListView
     */
    private var adapter: SimpleCursorAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this)

        // Gets a CursorAdapter
        val activity: Activity = requireActivity()
        adapter = SimpleCursorAdapter(activity, R.layout.item_group, null, FROM_COLUMNS, TO_IDS, 0)

        // Gets the ListView from the View list of the parent activity
        val contactsListView = activity.findViewById<ListView>(android.R.id.list)

        // Sets the adapter for the ListView
        contactsListView.adapter = adapter

        // Set the item click listener to be the current fragment.
        contactsListView.onItemClickListener = this
    }

    // L O A D E R

    override fun onCreateLoader(loaderId: Int, args: Bundle?): Loader<Cursor> {
        var selection: String? = null
        val groups = getStringsPref(requireActivity(), Settings.PREF_FILTER_GROUPS)
        if (groups != null) {
            selection = ContactsContract.Groups._ID + " IN " + groups
        }
        val sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC"

        // Starts the query
        val activity: Activity = requireActivity()
        return CursorLoader(
            activity,
            treebolic.provider.contacts.Settings.GROUP_URI,
            treebolic.provider.contacts.Settings.GROUP_PROJECTION,
            selection,
            null,
            sort
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        if (cursor.isClosed)
            return
        Log.d(TAG, "count=" + cursor.count)

        // Put the result Cursor in the adapter for the ListView
        adapter!!.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Delete the reference to the existing Cursor
        adapter!!.swapCursor(null)
    }

    // C L I C K

    override fun onItemClick(parent: AdapterView<*>, item: View, position: Int, rowID: Long) {
        // Get the Cursor
        val adapter = parent.adapter as SimpleCursorAdapter
        val cursor = adapter.cursor

        Log.d(TAG, cursor.columnNames.joinToString(separator = ",", prefix = "{", postfix = "}"))

        // Move to the selected contact
        cursor.moveToPosition(position)

        // Get the _ID value
        val groupId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ID_INDEX)

        // Content Uri
        val systemId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_SYSTEM_ID_INDEX)
        val title = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_INDEX)
        val account = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_NAME)
        val accountType = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_TYPE)

        // Buffer
        val sb = XSpannableStringBuilder()
            .append("Group ID", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(groupId)
            .append('\n')
            .append("systemId ID", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(systemId ?: "null")
            .append('\n')
            .append("Title", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(title ?: "null")
            .append('\n')
            .append("Account", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(account ?: "null")
            .append('\n')
            .append("Account type", StyleSpan(Typeface.BOLD), 0)
            .append('\n')
            .append(accountType ?: "null")
            .append('\n')

        // view
        @SuppressLint("InflateParams") val dialogView = layoutInflater.inflate(R.layout.detail_contact, null)
        val textView = dialogView.findViewById<TextView>(android.R.id.text1)
        val imageView = dialogView.findViewById<ImageView>(R.id.photo)

        textView.text = sb
        imageView.setImageResource(R.drawable.ic_group_large)

        makeDialog(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    companion object {

        private const val TAG = "ListGroupsF"

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val FROM_COLUMNS = arrayOf(ContactsContract.Groups._ID, ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME, ContactsContract.Groups.ACCOUNT_TYPE)

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val TO_IDS = intArrayOf(R.id.group_id, R.id.display_name, R.id.account_name, R.id.account_type)
    }
}