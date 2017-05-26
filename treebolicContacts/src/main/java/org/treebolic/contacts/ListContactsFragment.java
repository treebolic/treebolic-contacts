package org.treebolic.contacts;

import android.content.ContentResolver;
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

import org.treebolic.TreebolicIface;

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
		final String source = Settings.getStringPref(getContext(), TreebolicIface.PREF_SOURCE);
		final String[] selectionArgs = {source};
		final boolean nullSource = source == null || source.isEmpty();
		selectionArgs[0] = nullSource ? null : "%" + source + "%";

		selectionArgs[0] = source == null || source.isEmpty() ? null : "%" + source + "%";

		// Starts the query
		return new CursorLoader(getActivity(), //
				ContactsContract.Contacts.CONTENT_URI, //
				Settings.PROJECTION, //
				nullSource ? null : Settings.SELECTION_DN_LIKE, //
				nullSource ? null : selectionArgs, //
				Settings.sortOrder);
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
			final String photoUriString = cursor.getString(Settings.PHOTO_URI_INDEX);
			final String photoThumbnailUriString = cursor.getString(Settings.PHOTO_THUMBNAIL_URI_INDEX);
			if (photoThumbnailUriString != null)
			{
				final Uri photoUri = Uri.parse(photoThumbnailUriString);
				Log.d(TAG, "thumbnail of " + name + " is " + photoThumbnailUriString);
				imageView.setImageURI(photoUri);
			}
			else if (photoUriString != null)
			{
				final Uri photoUri = Uri.parse(photoUriString);
				Log.d(TAG, "photo of " + name + " is " + photoUriString);
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

		final String[] columns = cursor.getColumnNames();
		Log.d(TAG, join(columns));

		// Move to the selected contact
		cursor.moveToPosition(position);

		// Get the _ID value
		final long contactId = cursor.getLong(Settings.ID_INDEX);

		// Get the selected LOOKUP KEY
		// final String contactKey = cursor.getString(Settings.LOOKUP_KEY_INDEX);

		// Lookup Uri. You can use it as the content URI for retrieving the details for a contact
		// final Uri contactLookupUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

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

		// Photo Uri
		//final Uri photoUriFromContent = Uri.withAppendedPath(contactContentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		//final Uri photoUriFromPhotoId = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);

		final XSpannableStringBuilder sb = new XSpannableStringBuilder();
		sb.append("ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(Long.toString(contactId)).append('\n');

		// info
		getNames(contactId, sb);
		getPhones(contactId, sb);
		getEmails(contactId, sb);
		getAddresses(contactId, sb);

		/*
		sb.append("key", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactKey) //
				.append('\n');

		sb.append("lookup Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactLookupUri == null ? null : contactLookupUri.toString()) //
				.append('\n');
		*/
		sb.append("content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(contactContentUri == null ? null : contactContentUri.toString()) //
				.append('\n');

		if (thumbnailUri != null)
		{
			sb.append("thumbnail Uri", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(thumbnailUri == null ? "null" : thumbnailUri.toString()) //
					.append('\n');
		}

		if (photoUri != null)
		{
			sb.append("photo Uri", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(photoUri == null ? "null" : photoUri.toString()) //
					.append('\n');
		}

		if (thumbnailId != 0)
		{
			sb.append("thumbnail id", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(Long.toString(thumbnailId)) //
					.append('\n');
		}

		if (photoId != 0)
		{
			sb.append("photo id", new StyleSpan(Typeface.BOLD), 0) //
					.append('\n') //
					.append(Long.toString(photoId)) //
					.append('\n');
		}

		/*
		sb.append("photo Uri From Content Uri", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUriFromContent == null ? "null" : photoUriFromContent.toString()) //
				.append('\n');

		sb.append("photo Uri From Photo Id", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(photoUriFromPhotoId == null ? "null" : photoUriFromPhotoId.toString()) //
				.append('\n');
		*/

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
		else if (thumbnailUri != null)
		{
			imageView.setImageURI(Uri.parse(thumbnailUri));
		}
		else
		{
			imageView.setImageResource(R.drawable.ic_contact_default);
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

	// M O R E

	private void getNames(final long id, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, Long.toString(id)}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxGiven = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME); // first
				final int idxFamily = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME); // last
				final int idxDisplay = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME); // display

				sb.append("names", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
				{
					final String given = cursor.getString(idxGiven);
					final String family = cursor.getString(idxFamily);
					final String display = cursor.getString(idxDisplay);
					sb.append(given) //
							.append('\n');
					sb.append(family) //
							.append('\n');
					sb.append(display) //
							.append('\n');
				}
			}
			cursor.close();
		}
	}

	private void getAddresses(final long id, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, Long.toString(id)}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxFormatted = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
				final int idxStreet = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
				final int idxCity = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
				final int idxPostCode = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
				final int idxCountry = cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);

				while (cursor.moveToNext())
				{
					sb.append("address", new StyleSpan(Typeface.BOLD), 0) //
							.append('\n');

					final String formatted = cursor.getString(idxFormatted);
					if (formatted != null)
					{
						sb.append(formatted) //
								.append(" (formatted)\n");
					}
					final String street = cursor.getString(idxStreet);
					if (street != null)
					{
						sb.append(street) //
								.append(" (street)\n");
					}
					final String city = cursor.getString(idxCity);
					if (city != null)
					{
						sb.append(city) //
								.append(" (city)\n");
					}
					final String postCode = cursor.getString(idxPostCode);
					if (postCode != null)
					{
						sb.append(postCode) //
								.append(" (postcode)\n");
					}
					final String country = cursor.getString(idxCountry);
					if (country != null)
					{
						sb.append(country) //
								.append(" (country)\n");
					}
				}
			}
			cursor.close();
		}
	}

	private void getEmails(final long id, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, Long.toString(id)}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxEmail = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);

				sb.append("email", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
				{
					String email = cursor.getString(idxEmail);
					sb.append(email) //
							.append('\n');
				}
			}
			cursor.close();
		}
	}

	private void getPhones(long contactId, final XSpannableStringBuilder sb)
	{
		final Context context = getContext();
		final ContentResolver cr = context.getContentResolver();
		final Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.Data.MIMETYPE + " = ?" + " AND " + ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, Long.toString(contactId)}, null);
		if (cursor != null)
		{
			if (cursor.getCount() > 0)
			{
				final int idxPhoneNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				final int idxPhoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
				sb.append("phone", new StyleSpan(Typeface.BOLD), 0) //
						.append('\n');

				while (cursor.moveToNext())
				{
					final String phoneNumber = idxPhoneNumber == -1 ? "" : cursor.getString(idxPhoneNumber);
					final int phoneType = idxPhoneNumber == -1 ? -1 : cursor.getInt(idxPhoneType);

					// phone
					switch (phoneType)
					{
						case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
							sb.append(phoneNumber) //
									.append(" (home)\n");
							break;
						case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
							sb.append(phoneNumber) //
									.append(" (mobile)\n");
							break;
						case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
							sb.append(phoneNumber) //
									.append(" (work)\n");
							break;
						default:
						case -1:
							sb.append(phoneNumber) //
									.append(" (other)\n");
							break;
					}
				}
			}
			cursor.close();
		}
	}

	private void str2sb(final String header, final String[] strs, final XSpannableStringBuilder sb)
	{
		sb.append(header, new StyleSpan(Typeface.BOLD), 0).append('\n');
		sb.append(join(strs)).append('\n');
	}

	private String join(final String[] strs)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('{');
		for (String str : strs)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(str);
		}
		sb.append('}');
		return sb.toString();
	}
}