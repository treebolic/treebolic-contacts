/*
 * Copyright (c) 2019-2023. Bernard Bou
 */

package org.treebolic.contacts;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.treebolic.TreebolicIface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

/**
 * Fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class ListContactsFragment extends AbstractListContactsCursorFragment implements LoaderManager.LoaderCallbacks<Cursor>
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
	private final static String[] FROM_COLUMNS_ALT = {ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE};

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static int[] TO_IDS = {R.id.display_name};

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	private SimpleCursorAdapter adapter;

	// C R E A T E

	/**
	 * Constructor
	 */
	public ListContactsFragment()
	{
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

		// Initializes the loader
		LoaderManager.getInstance(this).initLoader(0, null, this);

		// Gets adapter's source columns
		final String displayPref = Settings.getStringPref(requireContext(), Settings.PREF_DISPLAY);
		final String firstLastValue = getString(R.string.pref_display_value_first_last);
		final int displayCode = firstLastValue.equals(displayPref) ? 1 : 2;
		final String[] from = displayCode == 1 ? FROM_COLUMNS : FROM_COLUMNS_ALT;

		// Gets a CursorAdapter
		final Context context = getActivity();
		assert context != null;
		this.adapter = new ImageCursorAdapter(context, R.layout.item_contact, null, from, TO_IDS, 0);

		// Gets the ListView from the View list of the parent activity
		final Activity activity = getActivity();
		assert activity != null;
		final ListView contactsListView = activity.findViewById(android.R.id.list);

		// Sets the adapter for the ListView
		contactsListView.setAdapter(this.adapter);
	}

	// L O A D E R

	@NonNull
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		final String source = Settings.getStringPref(requireContext(), TreebolicIface.PREF_SOURCE);
		final boolean nullSource = source == null || source.isEmpty();

		String selection = nullSource ? null : treebolic.provider.contacts.Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};

		final String sortPref = Settings.getStringPref(requireContext(), Settings.PREF_SORT);
		final String firstValue = getString(R.string.pref_sort_value_first);
		final int sortCode = firstValue.equals(sortPref) ? 1 : 2;
		String sort = sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";

		// Starts the query
		final Activity activity = getActivity();
		assert activity != null;
		return new CursorLoader(activity, //
				treebolic.provider.contacts.Settings.CONTACT_URI, //
				treebolic.provider.contacts.Settings.CONTACT_PROJECTION, //
				selection, //
				selectionArgs, //
				sort);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, @NonNull Cursor cursor)
	{
		Log.d(TAG, "count=" + cursor.getCount());
		// dumpCursor(cursor);

		// Put the result Cursor in the adapter for the ListView
		this.adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader)
	{
		// Delete the reference to the existing Cursor
		this.adapter.swapCursor(null);
	}
}