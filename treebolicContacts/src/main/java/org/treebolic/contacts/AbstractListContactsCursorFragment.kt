/*
 * Copyright (c) 2026. Bernard Bou
 */
package org.treebolic.contacts

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.core.net.toUri
import treebolic.provider.contacts.Settings
import treebolic.provider.contacts.Utils.queryBitmapFromUri
import treebolic.provider.contacts.Utils.roundCrop

/**
 * Abstract fragment with list of contacts
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
abstract class AbstractListContactsCursorFragment : AbstractListContactsFragment(), OnItemClickListener {

    // A D A P T E R

    internal class ImageCursorAdapter(context: Context, layout: Int, cursor: Cursor?, from: Array<String>, to: IntArray?, flags: Int) : SimpleCursorAdapter(context, layout, cursor, from, to, flags) {

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            super.bindView(view, context, cursor)

            // Photo Uri
            val imageView = view.findViewById<ImageView>(R.id.photo)
            val photoThumbnailUri = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX)
            if (photoThumbnailUri != null) {
                Log.d(TAG, "thumbnail is $photoThumbnailUri")
                //imageView.setImageURI(Uri.parse(photoThumbnailUri))
                imageView.setImageBitmap(uri2CircularBitmap(context, photoThumbnailUri))
                return
            }

            val photoUri = cursor.getString(Settings.PHOTO_URI_INDEX)
            if (photoUri != null) {
                Log.d(TAG, "photo is $photoUri")
                //imageView.setImageURI(Uri.parse(photoUri))
                imageView.setImageBitmap(uri2CircularBitmap(context, photoUri))
                return
            }

            imageView.setImageResource(R.drawable.ic_contact)
        }

        private fun uri2CircularBitmap(context: Context, photoUriString: String): Bitmap {
            val bitmap = queryBitmapFromUri(context, photoUriString)!!
            return roundCrop(bitmap)
        }
    }

    // O V E R R I D E S

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gets the ListView from the View list of the parent activity
        val activity: Activity = requireActivity()
        val contactsListView = activity.findViewById<ListView>(android.R.id.list)

        // Set the item click listener to be the current fragment.
        contactsListView.onItemClickListener = this
    }

    // C L I C K

    override fun onItemClick(parent: AdapterView<*>, item: View, position: Int, rowID: Long) {
        // Get the Cursor
        val adapter = parent.adapter as SimpleCursorAdapter
        val cursor = adapter.cursor

        // Move to the selected contact
        cursor.moveToPosition(position)

        // Get the _ID value
        val contactId = cursor.getString(Settings.CONTACT_ID_INDEX)

        // Content Uri
        val contactContentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong())

        // IMAGES

        // Get the selected thumbnail Uri
        val thumbnailUri = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX)

        // Get the selected photo Uri
        val photoUri = cursor.getString(Settings.PHOTO_URI_INDEX)

        // Get the selected photo index
        val photoId = cursor.getString(Settings.PHOTO_ID_INDEX)

        // Get the selected thumbnail index
        val thumbnailId = cursor.getString(Settings.PHOTO_THUMBNAIL_ID_INDEX)

        // Photo Uri
        val sb = XSpannableStringBuilder()
        val labels = arrayOf("content Uri", "thumbnail Uri", "photo Uri", "thumbnail id", "photo id")
        showDialog(
            sb,  
            contactId,  
            thumbnailUri?.toUri() ?: photoUri?.toUri(),  
            labels,  
            contactContentUri.toString(),  
            thumbnailUri,  
            photoUri,  
            thumbnailId,  
            photoId
        )
    }

    // C U R S O R

    private fun dumpCursor(cursor: Cursor?) {
        if (cursor == null) {
            return
        }
        val columns = cursor.columnNames
        val indexes = IntArray(columns.size)
        for (i in indexes.indices) {
            indexes[i] = cursor.getColumnIndex(columns[i])
        }
        cursor.moveToFirst()
        do {
            for (i in indexes.indices) {
                val value = cursor.getString(indexes[i])
                Log.d(TAG, columns[i] + " " + value)
            }
            Log.d(TAG, "\n")
        } while (cursor.moveToNext())
        cursor.moveToFirst()
    }

    companion object {

        private const val TAG = "AListContactsF"
    }
}