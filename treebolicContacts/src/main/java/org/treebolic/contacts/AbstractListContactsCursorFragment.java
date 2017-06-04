package org.treebolic.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Abstract fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
abstract public class AbstractListContactsCursorFragment extends AbstractListContactsFragment implements AdapterView.OnItemClickListener
{
	static private final String TAG = "AListContactsF";

	// A D A P T E R

	class ImageCursorAdapter extends SimpleCursorAdapter
	{
		public ImageCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags)
		{
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			super.bindView(view, context, cursor);
			
			// Photo Uri
			final String photoThumbnailUriString = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_THUMBNAIL_URI_INDEX);
			if (photoThumbnailUriString != null)
			{
				final Uri photoUri = Uri.parse(photoThumbnailUriString);
				Log.d(TAG, "thumbnail is " + photoThumbnailUriString);
				final ImageView imageView = (ImageView) view.findViewById(R.id.photo);
				imageView.setImageURI(photoUri);
			}
			else
			{
				final String photoUriString = cursor.getString(treebolic.provider.contacts.Settings.PHOTO_URI_INDEX);
				if (photoUriString != null)
				{
					final Uri photoUri = Uri.parse(photoUriString);
					Log.d(TAG, "photo is " + photoUriString);
					final ImageView imageView = (ImageView) view.findViewById(R.id.photo);
					imageView.setImageURI(photoUri);
				}
			}
		}
	}

	// C R E A T E

	public AbstractListContactsCursorFragment()
	{
	}

	// O V E R R I D E S

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		// Always call the super method first
		super.onActivityCreated(savedInstanceState);

		// Gets the ListView from the View list of the parent activity
		final ListView contactsListView = (ListView) getActivity().findViewById(android.R.id.list);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	// C L I C K

	@Override
	public void onItemClick(AdapterView<?> parent, View item, int position, long rowID)
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
		final Uri contactContentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactId));

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

	private void dumpCursor(final Cursor cursor)
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