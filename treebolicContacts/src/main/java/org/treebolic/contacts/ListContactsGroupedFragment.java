/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.contacts;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.treebolic.TreebolicIface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import treebolic.provider.contacts.Utils;

/**
 * Fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsGroupedFragment extends AbstractListContactsFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
	static private final String TAG = "ListContactsGroupedF";

	// C U R S O R  C O L U M N S

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	@NonNull
	static private final String[] FROM_COLUMNS;

	/**
	 * Column names in the Cursor mapping to the ListView ids (alternative)
	 */
	@NonNull
	static private final String[] FROM_COLUMNS_ALT;

	/*
	 * Mapped-to ListView ids
	 */
	@NonNull
	static private final int[] TO_IDS;

	static
	{
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
		{
			FROM_COLUMNS = new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,};
			FROM_COLUMNS_ALT = new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
					ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,};
			TO_IDS = new int[]{R.id.display_name, R.id.group, R.id.account};
		}
		else
		{
			FROM_COLUMNS = new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,};
			FROM_COLUMNS_ALT = new String[]{ //
					ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
					ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,};
			TO_IDS = new int[]{R.id.display_name, R.id.group};
		}
	}

	// A D A P T E R  K E Y S

	/*
	 * Key names in the row mapping to the ListView ids
	 */
	private final static String CONTACT_ID_KEY = "contact_id";
	private final static String DISPLAY_NAME_KEY = "display_name";
	private final static String GROUP_KEY = "group";
	private final static String ACCOUNT_KEY = "account";
	private final static String PHOTO_THUMBNAIL_URI_KEY = "thumbnail_photo_uri";
	private final static String PHOTO_URI_KEY = "photo_uri";
	private final static String PHOTO_THUMBNAIL_ID_KEY = "thumbnail_photo_id";
	private final static String PHOTO_ID_KEY = "photo_id";
	private final static String[] FROM_KEYS = {DISPLAY_NAME_KEY, GROUP_KEY, ACCOUNT_KEY};

	/**
	 * Row of objects
	 *
	 * @param <T> object type
	 */
	private static class Row<T> extends HashMap<String, T>
	{
	}

	/**
	 * Simple adapter with image
	 */
	class ImageAdapter extends SimpleAdapter
	{
		@SuppressWarnings("WeakerAccess")
		public ImageAdapter(final Context context, final List<? extends Map<String, ?>> data, @SuppressWarnings("SameParameterValue") final int resource, @SuppressWarnings("SameParameterValue") final String[] from, @SuppressWarnings("SameParameterValue") final int[] to)
		{
			super(context, data, resource, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Log.d(TAG, "POS " + position);
			final View view = super.getView(position, convertView, parent);
			final ImageView imageView = view.findViewById(R.id.photo);

			@SuppressWarnings("unchecked") final HashMap<String, ?> item = (HashMap<String, ?>) this.getItem(position);
			if (item.containsKey(PHOTO_THUMBNAIL_URI_KEY))
			{
				final String thumbnailPhotoUri = (String) item.get(PHOTO_THUMBNAIL_URI_KEY);
				//imageView.setImageURI(Uri.parse(thumbnailPhotoUri));
				imageView.setImageBitmap(uri2CircularBitmap(thumbnailPhotoUri));
			}
			else if (item.containsKey(PHOTO_URI_KEY))
			{
				final String photoUri = (String) item.get(PHOTO_URI_KEY);
				//imageView.setImageURI(Uri.parse(photoUri));
				imageView.setImageBitmap(uri2CircularBitmap(photoUri));
			}
			else
			{
				imageView.setImageResource(R.drawable.ic_contact);
			}
			return view;
		}

		private Bitmap uri2CircularBitmap(final String photoUriString)
		{
			final Context context = getActivity();
			assert context != null;
			final Bitmap bitmap = Utils.queryBitmapFromUri(context, photoUriString);
			assert bitmap != null;
			return Utils.roundCrop(bitmap);
		}
	}

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	@Nullable
	private SimpleAdapter adapter;

	/**
	 * Data
	 */
	@NonNull
	private final List<Row<String>> rows;

	/**
	 * Groups
	 */
	@Nullable
	private Map<String, Utils.Group> groups;

	// C R E A T E

	/**
	 * Constructor
	 */
	public ListContactsGroupedFragment()
	{
		this.rows = new ArrayList<>();
	}

	// O V E R R I D E S

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// Gets the ListView from the View list of the parent activity
		final Activity activity = getActivity();
		assert activity != null;
		final ListView contactsListView = activity.findViewById(android.R.id.list);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);

		// Initializes the loader
		LoaderManager.getInstance(this).initLoader(0, null, this);

		// Gets a CursorAdapter
		this.adapter = new ImageAdapter(activity, this.rows, R.layout.item_contact_grouped, FROM_KEYS, TO_IDS);

		// Sets the adapter for the ListView
		contactsListView.setAdapter(this.adapter);

		// Get groups
		this.groups = treebolic.provider.contacts.Utils.queryGroups(activity);
	}

	// L O A D E R

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		// selection
		final String source = Settings.getStringPref(requireContext(), TreebolicIface.PREF_SOURCE);
		final boolean nullSource = source == null || source.isEmpty();
		String selection = nullSource ? null : treebolic.provider.contacts.Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};
		String extraSelection = ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final String groups = Settings.getStringsPref(requireContext(), Settings.PREF_FILTER_GROUPS);
		if (groups != null)
		{
			extraSelection += " AND " + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groups;
		}
		selection = selection == null ? extraSelection : selection + " AND " + extraSelection;

		// sort
		final String sortPref = Settings.getStringPref(requireContext(), Settings.PREF_SORT);
		final String firstValue = getString(R.string.pref_sort_value_first);
		final int sortCode = firstValue.equals(sortPref) ? 1 : 2;
		String sort = sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";
		final String extraSort = ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID;
		sort = sort + "," + extraSort;

		Log.d(TAG, "selection=" + selection);
		final Activity activity = getActivity();
		assert activity != null;
		return new CursorLoader(activity, //
				treebolic.provider.contacts.Settings.GROUP_MEMBERS_URI, //
				treebolic.provider.contacts.Settings.GROUP_MEMBERS_PROJECTION, //
				selection, //
				selectionArgs, //
				sort);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, @NonNull Cursor cursor)
	{
		Log.d(TAG, "count=" + cursor.getCount());

		// dumpCursor(cursor);
		this.rows.clear();
		//noinspection ConstantConditions
		if (cursor != null)
		{
			// Gets adapter's source columns
			final String displayPref = Settings.getStringPref(requireContext(), Settings.PREF_DISPLAY);
			final String firstLastValue = getString(R.string.pref_display_value_first_last);
			final int displayCode = firstLastValue.equals(displayPref) ? 1 : 2;
			final String[] columns = displayCode == 1 ? FROM_COLUMNS : FROM_COLUMNS_ALT;

			final int[] columnIndexes = new int[columns.length];
			for (int i = 0; i < columnIndexes.length; i++)
			{
				columnIndexes[i] = cursor.getColumnIndex(columns[i]);
			}
			if (cursor.moveToFirst())
			{
				String contactIdCurrent = null;
				Row<String> row = null;
				do
				{
					final int contactIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID);
					final int groupIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID);
					final int photoThumbnailUriIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.PHOTO_THUMBNAIL_URI);
					final int photoUriIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.PHOTO_URI);
					assert contactIdIdx != -1;
					assert groupIdIdx != -1;
					assert photoThumbnailUriIdx != -1;
					assert photoUriIdx != -1;
					final String contactId = cursor.getString(contactIdIdx);
					final String groupId = cursor.getString(groupIdIdx);
					final String photoThumbnailUri = cursor.getString(photoThumbnailUriIdx);
					final String photoUri = cursor.getString(photoUriIdx);

					// contact id
					if (!contactId.equals(contactIdCurrent))
					{
						row = new Row<>();
						this.rows.add(row);
						contactIdCurrent = contactId;
						row.put(CONTACT_ID_KEY, contactId);
					}

					// photo
					if (photoThumbnailUri != null && !row.containsKey(PHOTO_THUMBNAIL_URI_KEY))
					{
						row.put(PHOTO_THUMBNAIL_URI_KEY, photoThumbnailUri);
					}
					if (photoUri != null && !row.containsKey(PHOTO_URI_KEY))
					{
						row.put(PHOTO_URI_KEY, photoUri);
					}

					// group
					assert this.groups != null;
					final Utils.Group group = this.groups.get(groupId);
					if (group != null)
					{
						for (int i = 0; i < columnIndexes.length; i++)
						{
							final String key = FROM_KEYS[i];
							String value = cursor.getString(columnIndexes[i]);
							if (GROUP_KEY.equals(key))
							{
								value = group.title;
							}
							else if (ACCOUNT_KEY.equals(key))
							{
								value = group.account;
							}
							Log.d(TAG, key + " (" + columns[i] + ") " + value);
							if (i > 0)
							{
								String value0 = row.get(key);
								if (value0 != null)
								{
									value = value0 + '\n' + value;
								}
							}
							row.put(key, value);
						}
					}
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}
		assert this.adapter != null;
		this.adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader)
	{
		// Delete the reference to the existing Cursor
		assert this.adapter != null;
		this.adapter.notifyDataSetInvalidated();
	}

	// C L I C K

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, View item, int position, long rowID)
	{
		// Get the Cursor
		final SimpleAdapter adapter = (SimpleAdapter) parent.getAdapter();
		@SuppressWarnings("unchecked") final Row<String> row = (Row<String>) adapter.getItem(position);

		// Get the _ID value
		final String contactId = row.get(CONTACT_ID_KEY);

		// Content Uri
		final Uri contactContentUri = contactId == null ? null : ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));

		// IMAGES

		// Get the selected thumbnail Uri
		final String thumbnailUri = row.get(PHOTO_THUMBNAIL_URI_KEY);

		// Get the selected photo Uri
		final String photoUri = row.get(PHOTO_URI_KEY);

		// Get the selected photo index
		final String photoId = row.get(PHOTO_ID_KEY);

		// Get the selected thumbnail index
		final String thumbnailId = row.get(PHOTO_THUMBNAIL_ID_KEY);

		// Photo Uri
		//final Uri photoUriFromContent = Uri.withAppendedPath(contactContentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		//final Uri photoUriFromPhotoId = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, photoId);

		final XSpannableStringBuilder sb = new XSpannableStringBuilder();
		final String[] labels = new String[]{"content Uri", "thumbnail Uri", "photo Uri", "thumbnail id", "photo id"};
		showDialog(sb, //
				contactId, //
				thumbnailUri != null ? Uri.parse(thumbnailUri) : (photoUri != null ? Uri.parse(photoUri) : null), //
				labels, //
				contactContentUri == null ? "" : contactContentUri.toString(), //
				thumbnailUri, //
				photoUri, //
				thumbnailId, //
				photoId);
	}
}