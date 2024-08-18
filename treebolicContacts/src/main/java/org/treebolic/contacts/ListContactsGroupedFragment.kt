/*
 * Copyright (c) 2019-2023. Bernard Bou
 */
package org.treebolic.contacts

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import org.treebolic.TreebolicIface
import org.treebolic.contacts.Settings.getStringPref
import org.treebolic.contacts.Settings.getStringsPref
import treebolic.provider.contacts.Utils
import treebolic.provider.contacts.Utils.queryBitmapFromUri
import treebolic.provider.contacts.Utils.queryGroups
import treebolic.provider.contacts.Utils.roundCrop

/**
 * Fragment with list of contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class ListContactsGroupedFragment : AbstractListContactsFragment(), LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    /**
     * Row of objects
     *
     * @param T object type
     */
    private class Row<T> : HashMap<String?, T>()

    /**
     * Simple adapter with image
     */
    internal inner class ImageAdapter(context: Context?, data: List<MutableMap<String?, *>?>?, resource: Int, from: Array<String>?, to: IntArray?) : SimpleAdapter(context, data, resource, from, to) {

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            Log.d(TAG, "POS $position")
            val view = super.getView(position, convertView, parent)
            val imageView = view.findViewById<ImageView>(R.id.photo)

            val item = getItem(position) as HashMap<*, *>
            if (item.containsKey(PHOTO_THUMBNAIL_URI_KEY)) {
                val thumbnailPhotoUri = item[PHOTO_THUMBNAIL_URI_KEY] as String?
                //imageView.setImageURI(Uri.parse(thumbnailPhotoUri));
                imageView.setImageBitmap(uri2CircularBitmap(thumbnailPhotoUri))
            } else if (item.containsKey(PHOTO_URI_KEY)) {
                val photoUri = item[PHOTO_URI_KEY] as String?
                //imageView.setImageURI(Uri.parse(photoUri));
                imageView.setImageBitmap(uri2CircularBitmap(photoUri))
            } else {
                imageView.setImageResource(R.drawable.ic_contact)
            }
            return view
        }

        private fun uri2CircularBitmap(photoUriString: String?): Bitmap {
            val context: Context = checkNotNull(activity)
            val bitmap = checkNotNull(queryBitmapFromUri(context, photoUriString))
            return roundCrop(bitmap)
        }
    }

    /**
     * Adapter that binds the result Cursor to the ListView
     */
    private var adapter: SimpleAdapter? = null

    /**
     * Data
     */
    private val rows: MutableList<Row<String?>?> = ArrayList()

    /**
     * Groups
     */
    private var groups: Map<String, Utils.Group>? = null

    // O V E R R I D E S

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gets the ListView from the View list of the parent activity
        val activity: Activity = requireActivity()
        val contactsListView = activity.findViewById<ListView>(android.R.id.list)

        // Set the item click listener to be the current fragment.
        contactsListView.onItemClickListener = this

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this)

        // Gets a CursorAdapter
        this.adapter = ImageAdapter(activity, this.rows, R.layout.item_contact_grouped, FROM_KEYS, TO_IDS)

        // Sets the adapter for the ListView
        contactsListView.adapter = adapter

        // Get groups
        this.groups = queryGroups(activity)
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

        // dumpCursor(cursor);
        rows.clear()
        // Gets adapter's source columns
        val displayPref = getStringPref(requireContext(), Settings.PREF_DISPLAY)
        val firstLastValue = getString(R.string.pref_display_value_first_last)
        val displayCode = if (firstLastValue == displayPref) 1 else 2
        val columns = if (displayCode == 1) FROM_COLUMNS else FROM_COLUMNS_ALT

        val columnIndexes = IntArray(columns.size)
        for (i in columnIndexes.indices) {
            columnIndexes[i] = cursor.getColumnIndex(columns[i])
        }
        if (cursor.moveToFirst()) {
            var contactIdCurrent: String? = null
            var row: Row<String?>? = null
            do {
                val contactIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID)
                val groupIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID)
                val photoThumbnailUriIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI)
                val photoUriIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI)
                assert(contactIdIdx != -1)
                assert(groupIdIdx != -1)
                assert(photoThumbnailUriIdx != -1)
                assert(photoUriIdx != -1)
                val contactId = cursor.getString(contactIdIdx)
                val groupId = cursor.getString(groupIdIdx)
                val photoThumbnailUri = cursor.getString(photoThumbnailUriIdx)
                val photoUri = cursor.getString(photoUriIdx)

                // contact id
                if (contactId != contactIdCurrent) {
                    row = Row()
                    rows.add(row)
                    contactIdCurrent = contactId
                    row[CONTACT_ID_KEY] = contactId
                }

                // photo
                if (photoThumbnailUri != null && !row!!.containsKey(PHOTO_THUMBNAIL_URI_KEY)) {
                    row[PHOTO_THUMBNAIL_URI_KEY] = photoThumbnailUri
                }
                if (photoUri != null && !row!!.containsKey(PHOTO_URI_KEY)) {
                    row[PHOTO_URI_KEY] = photoUri
                }

                // group
                checkNotNull(this.groups)
                val group = groups!![groupId]
                if (group != null) {
                    for (i in columnIndexes.indices) {
                        val key = FROM_KEYS[i]
                        var value = cursor.getString(columnIndexes[i])
                        if (GROUP_KEY == key) {
                            value = group.title
                        } else if (ACCOUNT_KEY == key) {
                            value = group.account
                        }
                        Log.d(TAG, key + " (" + columns[i] + ") " + value)
                        if (i > 0) {
                            val value0 = row!![key]
                            if (value0 != null) {
                                value = """
                                    $value0
                                    $value
                                    """.trimIndent()
                            }
                        }
                        row!![key] = value
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        checkNotNull(this.adapter)
        adapter!!.notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Delete the reference to the existing Cursor
        checkNotNull(this.adapter)
        adapter!!.notifyDataSetInvalidated()
    }

    // C L I C K

    override fun onItemClick(parent: AdapterView<*>, item: View, position: Int, rowID: Long) {
        // Get the Cursor
        val adapter = parent.adapter as SimpleAdapter
        @Suppress("UNCHECKED_CAST")
        val row = adapter.getItem(position) as Row<String?>

        // Get the _ID value
        val contactId = row[CONTACT_ID_KEY]

        // Content Uri
        val contactContentUri = if (contactId == null) null else ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())

        // IMAGES

        // Get the selected thumbnail Uri
        val thumbnailUri = row[PHOTO_THUMBNAIL_URI_KEY]

        // Get the selected photo Uri
        val photoUri = row[PHOTO_URI_KEY]

        // Get the selected photo index
        val photoId = row[PHOTO_ID_KEY]

        // Get the selected thumbnail index
        val thumbnailId = row[PHOTO_THUMBNAIL_ID_KEY]

        // Photo Uri
        val sb = XSpannableStringBuilder()
        val labels = arrayOf("content Uri", "thumbnail Uri", "photo Uri", "thumbnail id", "photo id")
        showDialog(
            sb,  //
            contactId!!,  //
            if (thumbnailUri != null) Uri.parse(thumbnailUri) else (if (photoUri != null) Uri.parse(photoUri) else null),  //
            labels,  //
            contactContentUri?.toString() ?: "",  //
            thumbnailUri,  //
            photoUri,  //
            thumbnailId,  //
            photoId
        )
    }

    companion object {

        private const val TAG = "ListContactsGroupedF"

        // C U R S O R  C O L U M N S

        /**
         * Column names in the Cursor mapping to the ListView ids
         */
        private val FROM_COLUMNS: Array<String>

        /**
         * Column names in the Cursor mapping to the ListView ids (alternative)
         */
        private val FROM_COLUMNS_ALT: Array<String>

        /*
	 * Mapped-to ListView ids
	 */
        private val TO_IDS: IntArray

        init {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                FROM_COLUMNS = arrayOf(
                    //
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,  //
                    ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,
                )
                FROM_COLUMNS_ALT = arrayOf(
                    //
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,  //
                    ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,
                )
                TO_IDS = intArrayOf(R.id.display_name, R.id.group, R.id.account)
            } else {
                FROM_COLUMNS = arrayOf(
                    //
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                )
                FROM_COLUMNS_ALT = arrayOf(
                    //
                    ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE,  //
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                )
                TO_IDS = intArrayOf(R.id.display_name, R.id.group)
            }
        }

        // A D A P T E R  K E Y S

        /*
	 * Key names in the row mapping to the ListView ids
	 */
        private const val CONTACT_ID_KEY = "contact_id"
        private const val DISPLAY_NAME_KEY = "display_name"
        private const val GROUP_KEY = "group"
        private const val ACCOUNT_KEY = "account"
        private const val PHOTO_THUMBNAIL_URI_KEY = "thumbnail_photo_uri"
        private const val PHOTO_URI_KEY = "photo_uri"
        private const val PHOTO_THUMBNAIL_ID_KEY = "thumbnail_photo_id"
        private const val PHOTO_ID_KEY = "photo_id"
        private val FROM_KEYS = arrayOf(DISPLAY_NAME_KEY, GROUP_KEY, ACCOUNT_KEY)
    }
}