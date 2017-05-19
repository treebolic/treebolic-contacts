package org.treebolic.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import treebolic.provider.contacts.Settings;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
	static private final String TAG = "ListContactsF";

	/**
	 * Column names in the Cursor mapping to the ListView ids.
	 */
	private final static String[] FROM_COLUMNS = {ContactsContract.Contacts.DISPLAY_NAME};

	/**
	 * Column names in the Cursor mapping to the ListView ids.
	 */
	private final static int[] TO_IDS = {android.R.id.text1};

	// V I E W

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	private SimpleCursorAdapter adapter;

	// C R E A T E

	public ListContactsFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list_contacts, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState)
	{
		// Always call the super method first
		super.onActivityCreated(savedInstanceState);

		// Initializes the loader
		getLoaderManager().initLoader(0, null, this);

		// Gets the ListView from the View list of the parent activity
		/*
	  ListView object
	 */
		ListView contactsListView = (ListView) getActivity().findViewById(android.R.id.list);

		// Gets a CursorAdapter
		this.adapter = new ImageCursorAdapter(getActivity(), R.layout.item_contact, null, FROM_COLUMNS, TO_IDS, 0);

		// Sets the adapter for the ListView

		contactsListView.setAdapter(this.adapter);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		// Makes search string into pattern and stores it in the selection array
		Settings.selectionArgs[0] = "%" + Settings.searchString + "%";

		// Starts the query
		return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, Settings.PROJECTION, Settings.SELECTION, Settings.selectionArgs, Settings.sortOrder);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		// Put the result Cursor in the adapter for the ListView
		this.adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		// Delete the reference to the existing Cursor
		this.adapter.swapCursor(null);
	}

	public class ImageCursorAdapter extends SimpleCursorAdapter
	{
		public ImageCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags)
		{
			super(context, layout, cursor, from, to, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			super.bindView(view, context, cursor);

			final ImageView imageView = (ImageView) view.findViewById(R.id.photo);

			// Photo Uri
			final String name = cursor.getString(Settings.NAME_INDEX);
			final String photoTumbnailUriString = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);
			final String photoUriString = cursor.getString(Settings.PHOTO_URI_INDEX);
			if (photoTumbnailUriString != null)
			{
				final Uri photoUri = Uri.parse(photoTumbnailUriString);
				Log.d(TAG, "photoUri of " + name + " is " + photoTumbnailUriString);
				imageView.setImageURI(photoUri);
			}
			else
			{
				imageView.setImageResource(R.drawable.ic_contact);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View item, int position, long rowID)
	{
		// Get the Cursor
		final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
		final Cursor cursor = adapter.getCursor();

		// Move to the selected contact
		cursor.moveToPosition(position);

		// Get the _ID value
		long contactId = cursor.getLong(Settings.ID_INDEX);

		// Get the selected LOOKUP KEY
		String contactKey = cursor.getString(Settings.LOOKUP_KEY_INDEX);

		// Lookup Uri
		Uri contactLookupUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

		// Content Uri
		final Uri contentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

		// Photo Uri
		final Uri photoUri = Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

		// You can use contactLookupUri as the content URI for retrieving the details for a contact.

		final XSpannableStringBuilder sb = new XSpannableStringBuilder();
		sb.append("ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(Long.toString(contactId)).append('\n');

		sb.append("key", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactKey) //
				.append('\n');

		sb.append("lookup Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactLookupUri.toString()) //
				.append('\n');

		sb.append("content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contentUri.toString()) //
				.append('\n');

		sb.append("photo Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUri.toString()) //
				.append('\n');

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = (TextView) dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = (ImageView) dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		if (photoUri != null)
		{
			imageView.setImageURI(photoUri);
		}
		else
		{
			imageView.setImageResource(R.drawable.ic_contact);
		}

		builder.setView(dialogView).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				//do things
			}
		});
		builder.show();
	}


	// U T I L S

	class XSpannableStringBuilder extends SpannableStringBuilder
	{
		public SpannableStringBuilder append(CharSequence text, Object what, int flags)
		{
			int start = length();
			append(text);
			setSpan(what, start, length(), flags);
			return this;
		}
	}

	// final long contactId = Long.parseLong(contactIdString);
	/*
	final Long contactId = cursor.getLong(ID_INDEX);
	final Uri contentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
	final Uri photoUri = Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	*/
	// OR InputStream ContactsContract.Contacts.openContactPhotoInputStream (ContentResolver cr, Uri contactLookupUri);


	/**
	 * Get photo
	 *
	 * @return the photo URI
	 */
	public Uri getPhotoUri(final long contactId)
	{
		try
		{
			final Cursor cursor = getContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, //
					null, //
					ContactsContract.Data.CONTACT_ID + "=" + contactId + " AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", //
					null,  //
					null);
			if (cursor != null)
			{
				if (!cursor.moveToFirst())
				{
					return null; // no photo
				}
				cursor.close();
			}
			else
			{
				return null; // error in cursor process
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		final Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
	}

	/**
	 * Get thumbnail photo input stream
	 *
	 * @param contactId contact id
	 * @return input stream
	 */
	public InputStream openPhoto(final long contactId)
	{
		final Uri contentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		final Uri photoUri = Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		final Cursor cursor = getContext().getContentResolver().query(photoUri, new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
		if (cursor == null)
		{
			return null;
		}
		try
		{
			if (cursor.moveToFirst())
			{
				byte[] data = cursor.getBlob(0);
				if (data != null)
				{
					return new ByteArrayInputStream(data);
				}
			}
		}
		finally
		{
			cursor.close();
		}
		return null;
	}

	/**
	 * Get display photo input stream
	 *
	 * @param contactId contact od
	 * @return input stream (Photo.PHOTO -> ByteArrayInputStream)
	 */
	public InputStream openDisplayPhoto(final long contactId)
	{
		final Uri contentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
		final Uri photoUri = Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
		try
		{
			final AssetFileDescriptor fd = getContext().getContentResolver().openAssetFileDescriptor(photoUri, "r");
			if (fd != null)
			{
				return fd.createInputStream();
			}
		}
		catch (IOException e)
		{
			//
		}
		return null;
	}

	private static final String[] PHOTO_BITMAP_PROJECTION = new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO};

	/**
	 * Get thumbnail
	 *
	 * @param thumbnailId id as per ContactsContract.Contacts.PHOTO_ID
	 * @return bitmap
	 */
	final Bitmap getThumbnail(final int thumbnailId)
	{
		final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
		final Cursor cursor = getContext().getContentResolver().query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);
		if (cursor != null)
		{
			try
			{
				Bitmap thumbnail = null;
				if (cursor.moveToFirst())
				{
					final byte[] thumbnailBytes = cursor.getBlob(0);
					if (thumbnailBytes != null)
					{
						thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
					}
				}
				return thumbnail;
			}
			finally
			{
				cursor.close();
			}
		}
		return null;
	}

	/**
	 * Put photo to image view
	 *
	 * @param contactId contact id
	 * @param imageView image view
	 */
	void putPhoto(final long contactId, final ImageView imageView)
	{
		final Uri photoUri = getPhotoUri(contactId);
		if (photoUri != null)
		{
			imageView.setImageURI(photoUri);
		}
		else
		{
			imageView.setImageResource(R.drawable.ic_contact);
		}
	}

	/**
	 * Put photo to image view
	 *
	 * @param thumbnailId thumbnail id
	 * @param imageView   image view
	 */
	public void putPhotoBitmap(final int thumbnailId, final ImageView imageView)
	{
		if (thumbnailId != -1)
		{
			final Bitmap thumbnail = getThumbnail(thumbnailId);
			if (thumbnail != null)
			{
				imageView.setImageBitmap(thumbnail);
			}
		}
	}

	protected Bitmap getContactPhoto(Context context, Uri lookupUri)
	{
		InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), lookupUri);
		if (stream == null)
		{
			return null;
		}
		Bitmap resultPhoto = BitmapFactory.decodeStream(stream);
		try
		{
			stream.close();
		}
		catch (IOException e)
		{
			//
		}
		return resultPhoto;
	}

	// int columnLookupKey = cursor.getColumnIndex(Contacts.LOOKUP_KEY);
	// String lookupKey = cursor.getString(columnLookupKey);
	// Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
	// Bitmap contactPhoto = getContactPhoto(context, lookupUri);
}