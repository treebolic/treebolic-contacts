package org.treebolic.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
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

import treebolic.provider.contacts.Settings;

/**
 * Fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
	static private final String TAG = "ListContactsF";

	// A D A P T E R

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static String[] FROM_COLUMNS = {ContactsContract.Contacts.DISPLAY_NAME};

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static int[] TO_IDS = {android.R.id.text1};

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	private SimpleCursorAdapter adapter;

	// C R E A T E

	public ListContactsFragment()
	{
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list_contacts, container, false);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		// Always call the super method first
		super.onActivityCreated(savedInstanceState);

		// Initializes the loader
		getLoaderManager().initLoader(0, null, this);

		// Gets the ListView from the View list of the parent activity
		final ListView contactsListView = (ListView) getActivity().findViewById(android.R.id.list);

		// Gets a CursorAdapter
		this.adapter = new ImageCursorAdapter(getActivity(), R.layout.item_contact, null, FROM_COLUMNS, TO_IDS, 0);

		// Sets the adapter for the ListView

		contactsListView.setAdapter(this.adapter);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	// L O A D E R

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		// Makes search string into pattern and stores it in the selection array
		Settings.selectionArgs[0] = "%" + Settings.searchString + "%";

		// Starts the query
		return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI, Settings.PROJECTION, Settings.SELECTION, Settings.selectionArgs, Settings.sortOrder);
	}

	@Override
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

	// A D A P T E R

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
		final long contactId = cursor.getLong(Settings.ID_INDEX);

		// Get the selected LOOKUP KEY
		final String contactKey = cursor.getString(Settings.LOOKUP_KEY_INDEX);

		// Lookup Uri. You can use it as the content URI for retrieving the details for a contact
		final Uri contactLookupUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

		// Content Uri
		final Uri contactContentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

		// IMAGES

		// Get the selected thumbnail Uri
		final String thumbnailUri = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);

		// Get the selected photo Uri
		final String photoUri = cursor.getString(Settings.PHOTO_URI_INDEX);

		// Get the selected photo index
		final long photoId = cursor.getLong(Settings.PHOTO_ID_INDEX);

		// Get the selected thumbnail index
		final long thumbnailId = cursor.getLong(Settings.PHOTO_THUMBNAIL_ID_INDEX);

		// Thumbnail photo Uri
		final Uri photoUriFromContent = Uri.withAppendedPath(contactContentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		final Uri photoUriFromPhotoId = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);

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
				.append(contactLookupUri == null ? null : contactLookupUri.toString()) //
				.append('\n');

		sb.append("content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactContentUri == null ? null : contactContentUri.toString()) //
				.append('\n');

		sb.append("thumbnail Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(thumbnailUri == null ? "null" : thumbnailUri.toString()) //
				.append('\n');

		sb.append("thumbnail id", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(Long.toString(thumbnailId)) //
				.append('\n');

		sb.append("photo Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUri == null ? "null" : photoUri.toString()) //
				.append('\n');

		sb.append("photo id", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(Long.toString(photoId)) //
				.append('\n');

		sb.append("photo Uri From Content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUriFromContent == null ? "null" : photoUriFromContent.toString()) //
				.append('\n');

		sb.append("photo Uri From Photo Id", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUriFromPhotoId == null ? "null" : photoUriFromPhotoId.toString()) //
				.append('\n');

		// view
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = (TextView) dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = (ImageView) dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		if (photoUri != null)
		{
			imageView.setImageURI(Uri.parse(photoUri));
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
}