package org.treebolic.contacts;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.treebolic.TreebolicIface;

/**
 * Fragment with list of contacts
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ListContactsUnGroupedFragment extends AbstractListContactsCursorFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	static private final String TAG = "ListContactsUnGroupedF";

	// A D A P T E R

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static String[] FROM_COLUMNS = {ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME, //
			ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
			ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET, //
	};

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	private final static String[] FROM_COLUMNS_ALT = {ContactsContract.CommonDataKinds.GroupMembership.DISPLAY_NAME_ALTERNATIVE, //
			ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, //
			ContactsContract.CommonDataKinds.GroupMembership.ACCOUNT_TYPE_AND_DATA_SET,//
	};

	/**
	 * Column names in the Cursor mapping to the ListView ids
	 */
	private final static int[] TO_IDS = {R.id.display_name, R.id.group, R.id.account};

	/**
	 * Adapter that binds the result Cursor to the ListView
	 */
	private SimpleCursorAdapter adapter;

	/*
	 * Groups
	 */
	// private Map<String, Utils.Group> groups;

	// C R E A T E

	/**
	 * Constructor
	 */
	public ListContactsUnGroupedFragment()
	{
	}

	// O V E R R I D E S

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState)
	{
		// Always call the super method first
		super.onActivityCreated(savedInstanceState);

		// Initializes the loader
		getLoaderManager().initLoader(0, null, this);

		// Gets adapter's source columns
		final String displayPref = Settings.getStringPref(getContext(), Settings.PREF_DISPLAY);
		final String firstLastValue = getString(R.string.pref_display_value_first_last);
		final int displayCode = firstLastValue.equals(displayPref) ? 1 : 2;
		final String[] from = displayCode == 1 ? FROM_COLUMNS : FROM_COLUMNS_ALT;

		// Gets a CursorAdapter
		this.adapter = new ImageCursorAdapter(getActivity(), R.layout.item_contact_ungrouped, null, from, TO_IDS, 0);

		// Gets the ListView from the View list of the parent activity
		final ListView contactsListView = (ListView) getActivity().findViewById(android.R.id.list);

		// Sets the adapter for the ListView
		contactsListView.setAdapter(this.adapter);

		// Get groups
		// this.groups = treebolic.provider.contacts.Utils.queryGroups(getActivity());
	}

	// L O A D E R

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		// selection
		final String source = Settings.getStringPref(getContext(), TreebolicIface.PREF_SOURCE);
		final boolean nullSource = source == null || source.isEmpty();
		String selection = nullSource ? null : treebolic.provider.contacts.Settings.SELECTION_DN_LIKE;
		final String[] selectionArgs = nullSource ? null : new String[]{"%" + source + "%"};
		String extraSelection = ContactsContract.Data.MIMETYPE + " = " + "'" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
		final String groups = Settings.getStringsPref(getContext(), Settings.PREF_FILTER_GROUPS);
		if (groups != null)
		{
			extraSelection += " AND " + ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + " IN " + groups;
		}
		selection = selection == null ? extraSelection : selection + " AND " + extraSelection;

		// sort
		final String sortPref = Settings.getStringPref(getContext(), Settings.PREF_SORT);
		final String firstValue = getString(R.string.pref_sort_value_first);
		final int sortCode = firstValue.equals(sortPref) ? 1 : 2;
		String sort = sortCode == 1 ? ContactsContract.Contacts.DISPLAY_NAME : ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE + " COLLATE LOCALIZED ASC";
		final String extraSort = ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID;
		sort = sort + "," + extraSort;

		Log.d(TAG, "selection=" + selection);
		return new CursorLoader(getActivity(), //
				treebolic.provider.contacts.Settings.GROUP_MEMBERS_URI, //
				treebolic.provider.contacts.Settings.GROUP_MEMBERS_PROJECTION, //
				selection, //
				selectionArgs, //
				sort);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
	{
		Log.d(TAG, "count=" + cursor.getCount());
		// dumpCursor(cursor);

		// Put the result Cursor in the adapter for the ListView
		this.adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		// Delete the reference to the existing Cursor
		this.adapter.swapCursor(null);
	}
}