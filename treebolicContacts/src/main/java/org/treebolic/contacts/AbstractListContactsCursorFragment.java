/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import treebolic.provider.contacts.Utils;

/**
 * Abstract fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
abstract public class AbstractListContactsCursorFragment extends AbstractListContactsFragment implements AdapterView.OnItemClickListener
{
	static private final String TAG = "AListContactsF";

	// A D A P T E R

	static class ImageCursorAdapter extends SimpleCursorAdapter
	{
		@SuppressWarnings("WeakerAccess")
		public ImageCursorAdapter(@NonNull Context context, int layout, @SuppressWarnings("SameParameterValue") Cursor cursor, String[] from, int[] to, @SuppressWarnings("SameParameterValue") int flags)
		{
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(@NonNull View view, @NonNull Context context, @NonNull Cursor cursor)
		{
			super.bindView(view, context, cursor);

			// Photo Uri
			final ImageView imageView = view.findViewById(R.id.photo);
			final String photoThumbnailUri = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_THUMBNAIL_URI_INDEX);
			if (photoThumbnailUri != null)
			{
				Log.d(TAG, "thumbnail is " + photoThumbnailUri);
				//imageView.setImageURI(Uri.parse(photoThumbnailUri));
				imageView.setImageBitmap(uri2CircularBitmap(context, photoThumbnailUri));
				return;
			}

			final String photoUri = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_URI_INDEX);
			if (photoUri != null)
			{
				Log.d(TAG, "photo is " + photoUri);
				//imageView.setImageURI(Uri.parse(photoUri));
				imageView.setImageBitmap(uri2CircularBitmap(context, photoUri));
				return;
			}

			imageView.setImageResource(R.drawable.ic_contact);
		}

		private Bitmap uri2CircularBitmap(@NonNull final Context context, final String photoUriString)
		{
			final Bitmap bitmap = Utils.queryBitmapFromUri(context, photoUriString);
			assert bitmap != null;
			return Utils.roundCrop(bitmap);
		}
	}

	// C R E A T E

	@SuppressWarnings("WeakerAccess")
	public AbstractListContactsCursorFragment()
	{
	}

	// O V E R R I D E S

	@SuppressWarnings("WeakerAccess")
	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		// Always call the super method first
		super.onActivityCreated(savedInstanceState);

		// Gets the ListView from the View list of the parent activity
		final Activity activity = getActivity();
		assert activity != null;
		final ListView contactsListView = activity.findViewById(android.R.id.list);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	// C L I C K

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, View item, int position, long rowID)
	{
		// Get the Cursor
		final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
		final Cursor cursor = adapter.getCursor();

		// Move to the selected contact
		cursor.moveToPosition(position);

		// Get the _ID value
		final String contactId = cursor.getString(treebolic.provider.contacts.Settings.CONTACT_ID_INDEX);

		// Get the selected LOOKUP KEY
		// final String contactKey = cursor.getString(Settings.LOOKUP_KEY_INDEX);

		// Lookup Uri. You can use it as the content URI for retrieving the details for a contact
		// final Uri contactLookupUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

		// Content Uri
		final Uri contactContentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));

		// IMAGES

		// Get the selected thumbnail Uri
		final String thumbnailUri = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_THUMBNAIL_URI_INDEX);

		// Get the selected photo Uri
		final String photoUri = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_URI_INDEX);

		// Get the selected photo index
		final String photoId = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_ID_INDEX);

		// Get the selected thumbnail index
		final String thumbnailId = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_THUMBNAIL_ID_INDEX);

		// Photo Uri
		//final Uri photoUriFromContent = Uri.withAppendedPath(contactContentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		//final Uri photoUriFromPhotoId = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);

		final XSpannableStringBuilder sb = new XSpannableStringBuilder();
		final String[] labels = new String[]{"content Uri", "thumbnail Uri", "photo Uri", "thumbnail id", "photo id"};
		showDialog(sb, //
				contactId, //
				thumbnailUri != null ? Uri.parse(thumbnailUri) : (photoUri != null ? Uri.parse(photoUri) : null), //
				labels, //
				contactContentUri.toString(), //
				thumbnailUri, //
				photoUri, //
				thumbnailId, //
				photoId);
	}

	// C U R S O R

	private void dumpCursor(@Nullable final Cursor cursor)
	{
		if (cursor == null)
		{
			return;
		}
		final String[] columns = cursor.getColumnNames();
		final int[] indexes = new int[columns.length];
		for (int i = 0; i < indexes.length; i++)
		{
			indexes[i] = cursor.getColumnIndex(columns[i]);
		}
		cursor.moveToFirst();
		do
		{
			for (int i = 0; i < indexes.length; i++)
			{
				String value = cursor.getString(indexes[i]);
				Log.d(TAG, columns[i] + " " + value);
			}
			Log.d(TAG, "\n");
		}
		while (cursor.moveToNext());
		cursor.moveToFirst();
	}
}