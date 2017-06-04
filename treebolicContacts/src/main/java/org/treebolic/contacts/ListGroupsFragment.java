package org.treebolic.contacts;

import android.content.ContentUris;
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

		// Gets a CursorAdapter
		this.adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_group, null, FROM_COLUMNS, TO_IDS, 0);

		// Gets the ListView from the View list of the parent activity
		final ListView contactsListView = (ListView) getActivity().findViewById(android.R.id.list);

		// Sets the adapter for the ListView
		contactsListView.setAdapter(this.adapter);

		// Set the item click listener to be the current fragment.
		contactsListView.setOnItemClickListener(this);
	}

	// L O A D E R

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
	{
		String selection = null;
		final String groups = Settings.getStringsPref(getActivity(), Settings.PREF_FILTER_GROUPS);
		if (groups != null)
		{
			selection = ContactsContract.Groups._ID + " IN " + groups;
		}
		final String[] selectionArgs = null;
		final String sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		// Starts the query
		return new CursorLoader(getActivity(), //
				treebolic.provider.contacts.Settings.GROUP_URI, //
				treebolic.provider.contacts.Settings.GROUP_PROJECTION, //
				selection, //
				selectionArgs, //
				sort);
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

	// C L I C K

	@Override
	public void onItemClick(AdapterView<?> parent, View item, int position, long rowID)
	{
		// Get the Cursor
		final SimpleCursorAdapter adapter = (SimpleCursorAdapter) parent.getAdapter();
		final Cursor cursor = adapter.getCursor();

		Log.d(TAG, join(cursor.getColumnNames()));

		// Move to the selected contact
		cursor.moveToPosition(position);

		// Get the _ID value
		final long contactId = cursor.getLong(treebolic.provider.contacts.Settings.CONTACT_ID_INDEX);

		// Get the selected LOOKUP KEY
		// final String contactKey = cursor.getString(Settings.LOOKUP_KEY_INDEX);

		// Lookup Uri. You can use it as the content URI for retrieving the details for a contact
		// final Uri contactLookupUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

		// Content Uri
		final Uri contactContentUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

		// IMAGES

		final XSpannableStringBuilder sb = new XSpannableStringBuilder();
		sb.append("ID", new StyleSpan(Typeface.BOLD), 0) //
				.append('\n') //
				.append(Long.toString(contactId)).append('\n');

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


		// view
		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.detail_contact, null);
		final TextView textView = (TextView) dialogView.findViewById(android.R.id.text1);
		final ImageView imageView = (ImageView) dialogView.findViewById(R.id.photo);
		textView.setText(sb);
		imageView.setImageResource(R.drawable.ic_group_default);

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