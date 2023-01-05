/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.contacts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListGroupsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener
{
	static private final String TAG = "ListGroupsF";

	// A D A P T E R

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static String[] FROM_COLUMNS = {ContactsContract.Groups._ID, ContactsContract.Groups.TITLE, ContactsContract.Groups.ACCOUNT_NAME, ContactsContract.Groups.ACCOUNT_TYPE};

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static int[] TO_IDS = {R.id.group_id, R.id.display_name, R.id.account_name, R.id.account_type};

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	private SimpleCursorAdapter adapter;

	// C R E A T E

	public ListGroupsFragment()
	{
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// Initializes the loader
		LoaderManager.getInstance(this).initLoader(0, null, this);

		// Gets a CursorAdapter
		final Activity activity = getActivity();
		assert activity != null;
		this.adapter = new SimpleCursorAdapter(activity, R.layout.item_group, null, FROM_COLUMNS, TO_IDS, 0);

		// Gets the ListView from the View list of the parent activity
		final ListView contactsListView = activity.findViewById(android.R.id.list);

		// Sets the adapter for the ListView
		contactsListView.setAdapter(this.adapter);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	// L O A D E R

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		String selection = null;
		final String groups = Settings.getStringsPref(requireActivity(), Settings.PREF_FILTER_GROUPS);
		if (groups != null)
		{
			selection = ContactsContract.Groups._ID + " IN " + groups;
		}
		final String sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		// Starts the query
		final Activity activity = getActivity();
		assert activity != null;
		return new CursorLoader(activity, //
				treebolic.provider.contacts.Settings.GROUP_URI, //
				treebolic.provider.contacts.Settings.GROUP_PROJECTION, //
				selection, //
				null, //
				sort);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor)
	{
		// Put the result Cursor in the adapter for the ListView
		this.adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader)
	{
		// Delete the reference to the existing Cursor
		this.adapter.swapCursor(null);
	}

	// C L I C K

	@Override
	public void onItemClick(@NonNull AdapterView<?> parent, View item, int position, long rowID)
	{
		// Get the Cursor
		final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
		final Cursor cursor = adapter.getCursor();

		Log.d(TAG, join(cursor.getColumnNames()));

		// Move to the selected contact
		cursor.moveToPosition(position);

		// Get the _ID value
		final String groupId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ID_INDEX);

		// Content Uri
		final String systemId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_SYSTEM_ID_INDEX);
		final String title = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_INDEX);
		final String account = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_NAME);
		final String accountType = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_TYPE);
		// final String titleRes = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_RES_INDEX);
		// final String resPackage = cursor.getString(treebolic.provider.contacts.Settings.GROUP_RES_PACKAGE_INDEX);

		// Buffer
		final AbstractListContactsFragment.XSpannableStringBuilder sb = new AbstractListContactsFragment.XSpannableStringBuilder();
		sb.append("Group ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(groupId) //
				.append('\n');
		sb.append("systemId ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(systemId == null ? "null" : systemId) //
				.append('\n');
		sb.append("Title", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(title == null ? "null" : title) //
				.append('\n');
		sb.append("Account", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(account == null ? "null" : account) //
				.append('\n');
		sb.append("Account type", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(accountType == null ? "null" : accountType) //
				.append('\n');

		// view
		final Activity activity = getActivity();
		assert activity != null;
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final LayoutInflater inflater = activity.getLayoutInflater();
		@SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		imageView.setImageResource(R.drawable.ic_group_large);

		builder.setView(dialogView).setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
			//do things
		});
		builder.show();
	}

	// U T I L S

	// --Commented out by Inspection START (9/23/17 5:48 PM):
	//	private void str2sb(final String header, final String[] strings, final AbstractListContactsFragment.XSpannableStringBuilder sb)
	//	{
	//		sb.append(header, new StyleSpan(Typeface.BOLD), 0).append('\n');
	//		sb.append(join(strings)).append('\n');
	//	}
	// --Commented out by Inspection STOP (9/23/17 5:48 PM)

	@NonNull
	private String join(@NonNull final String[] strings)
	{
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append('{');
		for (String str : strings)
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