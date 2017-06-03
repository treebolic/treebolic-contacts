package org.treebolic.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.preference.MultiSelectListPreference;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Group preference
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
public class GroupPreference extends MultiSelectListPreference
{
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public GroupPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public GroupPreference(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public GroupPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public GroupPreference(Context context)
	{
		super(context);
		init(context);
	}

	private void init(Context context)
	{
		final String selection = null;
		final String[] selectionArgs = null;
		final String sort = ContactsContract.Groups.TITLE + " COLLATE LOCALIZED ASC";

		final ContentResolver contentResolver = context.getContentResolver();
		final Cursor cursor = contentResolver.query( //
				ContactsContract.Groups.CONTENT_URI, //
				treebolic.provider.contacts.Settings.GROUP_PROJECTION, //
				selection, //
				selectionArgs, //
				sort);

		final List<String> labels = new ArrayList<>();
		final List<String> values = new ArrayList<>();
		if (cursor != null)
		{
			if (cursor.moveToFirst())
			{
				do
				{
					final String groupId = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ID_INDEX);
					CharSequence title = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_INDEX);
					final String titleRes = cursor.getString(treebolic.provider.contacts.Settings.GROUP_TITLE_RES_INDEX);
					if (titleRes != null)
					{
						int resId = Integer.parseInt(titleRes);
						final String resPackage = cursor.getString(treebolic.provider.contacts.Settings.GROUP_RES_PACKAGE_INDEX);
						title = context.getPackageManager().getText(resPackage, resId, null);
					}
					if ("Starred in Android".equals(title))
					{
						title = "Starred";
					}
					final String accountName = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_NAME);
					final String accountType = cursor.getString(treebolic.provider.contacts.Settings.GROUP_ACCOUNT_TYPE);
					Log.d("group ", groupId + " " + title + " " + titleRes + " " + accountName + " " + accountType);

					labels.add(title + " " + accountName + " " + accountType);
					values.add(groupId);
				}
				while (cursor.moveToNext());
			}
			cursor.close();
		}

		setEntries(labels.toArray(new String[0]));
		setEntryValues(values.toArray(new String[0]));
	}
}
